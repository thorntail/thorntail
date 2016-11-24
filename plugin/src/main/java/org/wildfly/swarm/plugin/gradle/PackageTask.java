/**
 * Copyright 2015-2016 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wildfly.swarm.plugin.gradle;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.ResolvedArtifact;
import org.gradle.api.plugins.ApplicationPluginConvention;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.bundling.Jar;
import org.wildfly.swarm.fractionlist.FractionList;
import org.wildfly.swarm.tools.ArtifactSpec;
import org.wildfly.swarm.tools.BuildTool;
import org.wildfly.swarm.tools.DeclaredDependencies;
import org.wildfly.swarm.tools.PropertiesUtil;

/**
 * @author Bob McWhirter
 */
public class PackageTask extends DefaultTask {

    private BuildTool tool;

    private Jar jarTask;

    public Task jarTask(Jar jarTask) {
        this.jarTask = jarTask;

        return this;
    }


    @TaskAction
    public void packageForSwarm() throws Exception {
        final Project project = getProject();
        final SwarmExtension ext = (SwarmExtension) project.getExtensions().getByName("swarm");

        if (ext.getMainClassName() == null) {
            if (project.getConvention().getPlugins().containsKey("application")) {
                ApplicationPluginConvention app = (ApplicationPluginConvention) project.getConvention().getPlugins().get("application");
                ext.setMainClassName(app.getMainClassName());
            }
        }

        final Properties fromFile = new Properties();
        if (ext.getPropertiesFile() != null) {
            try {
                fromFile.putAll(PropertiesUtil.loadProperties(ext.getPropertiesFile()));
            } catch (IOException e) {
                getLogger().error("Failed to load properties from " + ext.getPropertiesFile(), e);
            }
        }

        this.tool = new BuildTool(new GradleArtifactResolvingHelper(project))
                .projectArtifact(project.getGroup().toString(), project.getName(), project.getVersion().toString(),
                        jarTask.getExtension(), jarTask.getArchivePath())
                .mainClass(ext.getMainClassName())
                .bundleDependencies(ext.getBundleDependencies())
                .executable(ext.getExecutable())
                .executableScript(ext.getExecutableScript())
                .properties(ext.getProperties())
                .properties(fromFile)
                .properties(PropertiesUtil.filteredSystemProperties(ext.getProperties(), false))
                .fractionList(FractionList.get())
                .fractionDetectionMode(BuildTool.FractionDetectionMode.when_missing)
                .additionalModules(ext.getModuleDirs().stream()
                        .filter(f -> f.exists())
                        .map(File::getAbsolutePath)
                        .collect(Collectors.toList()))
                .logger(new BuildTool.SimpleLogger() {
                    @Override
                    public void debug(String msg) {
                        getLogger().debug(msg);
                    }

                    @Override
                    public void info(String msg) {
                        getLogger().info(msg);
                    }

                    @Override
                    public void error(String msg) {
                        getLogger().error(msg);
                    }

                    @Override
                    public void error(String msg, Throwable t) {
                        getLogger().error(msg, t);
                    }
                });

        DeclaredDependencies declaredDependencies = new DeclaredDependencies();
        List<ArtifactSpec> explicitDependencies = new ArrayList<>();

        project.getConfigurations()
                .getByName("compile")
                .getAllDependencies()
                .forEach((artifact) -> {
                    String groupId = artifact.getGroup();
                    String artifactId = artifact.getName();
                    explicitDependencies.add(new ArtifactSpec("compile", groupId, artifactId, null, "jar", null, null));
                });

        project.getConfigurations()
                .getByName("compile")
                .getResolvedConfiguration()
                .getResolvedArtifacts()
                .forEach(e -> addDependency(declaredDependencies, explicitDependencies, e));

        tool.declaredDependencies(declaredDependencies);

        final Boolean bundleDependencies = ext.getBundleDependencies();
        if (bundleDependencies != null) {
            this.tool.bundleDependencies(bundleDependencies);
        }

        this.tool.build(project.getName(), project.getBuildDir().toPath().resolve("libs"));
    }

    private void addDependency(DeclaredDependencies declaredDependencies, final List<ArtifactSpec> explicitDependencies, final ResolvedArtifact gradleArtifact) {

        String groupId = gradleArtifact.getModuleVersion().getId().getGroup();
        String artifactId = gradleArtifact.getModuleVersion().getId().getName();
        String version = gradleArtifact.getModuleVersion().getId().getVersion();
        String extension = gradleArtifact.getExtension();
        String classifier = gradleArtifact.getClassifier();
        File file = gradleArtifact.getFile();

        boolean isExplicit = false;
        for (ArtifactSpec each : explicitDependencies) {
            if ( each.groupId().equals( groupId ) && each.artifactId().equals( artifactId ) ) {
                declaredDependencies.addExplicitDependency(new ArtifactSpec("compile", groupId, artifactId, version, extension, classifier, file));
                isExplicit = true;
                break;
            }
        }

        if(!isExplicit)
            declaredDependencies.addTransientDependency(new ArtifactSpec("compile", groupId, artifactId, version, extension, classifier, file));
    }
}
