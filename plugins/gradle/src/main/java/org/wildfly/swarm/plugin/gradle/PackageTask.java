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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.ResolvedConfiguration;
import org.gradle.api.artifacts.ResolvedDependency;
import org.gradle.api.plugins.ApplicationPluginConvention;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.bundling.Jar;
import org.gradle.api.tasks.SourceSet;
import org.wildfly.swarm.fractions.PropertiesUtil;
import org.wildfly.swarm.spi.meta.SimpleLogger;
import org.wildfly.swarm.tools.ArtifactSpec;
import org.wildfly.swarm.tools.BuildTool;
import org.wildfly.swarm.tools.DeclaredDependencies;

/**
 * @author Bob McWhirter
 */
public class PackageTask extends DefaultTask {

    private static final String MODULE_DIR_NAME = "modules";

    private BuildTool tool;

    private Jar jarTask;

    public Task jarTask(Jar jarTask) {
        this.jarTask = jarTask;

        return this;
    }

    @TaskAction
    public void packageForSwarm() throws Exception {
        final Project project = getProject();

        GradleArtifactResolvingHelper resolvingHelper = new GradleArtifactResolvingHelper(project);
        Properties propertiesFromExtension = getPropertiesFromExtension();
        List<File> moduleDirs = getModuleDirs();
        if (moduleDirs.isEmpty()) {
            Path resourcesOutputDir = project.getConvention()
                    .getPlugin(JavaPluginConvention.class)
                    .getSourceSets()
                    .findByName(SourceSet.MAIN_SOURCE_SET_NAME)
                    .getOutput()
                    .getResourcesDir()
                    .toPath()
                    .resolve(MODULE_DIR_NAME);
            if (Files.isDirectory(resourcesOutputDir)) {
                File moduleDir = resourcesOutputDir.toFile();

                moduleDirs.add(moduleDir);
            }
        }


        this.tool = new BuildTool(resolvingHelper)
                .projectArtifact(this.jarTask.getGroup().toString(), this.jarTask.getBaseName(), this.jarTask.getVersion(),
                                 getPackaging(), getProjectArtifactFile())
                .mainClass(getMainClassName())
                .bundleDependencies(getBundleDependencies())
                .executable(getExecutable())
                .executableScript(getExecutableScript())
                .properties(propertiesFromExtension)
                .properties(getPropertiesFromFile())
                .properties(PropertiesUtil.filteredSystemProperties(propertiesFromExtension, false))
                .fractionDetectionMode(getSwarmExtension().getFractionDetectMode())
                .hollow(getHollow())
                .additionalModules(moduleDirs.stream()
                                           .filter(File::exists)
                                           .map(File::getAbsolutePath)
                                           .collect(Collectors.toList()))
                .logger(new SimpleLogger() {
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

       /* project.getConfigurations()
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
                .forEach(e -> addDependency(declaredDependencies, explicitDependencies, e));*/

        ResolvedConfiguration resolvedConfiguration = project.getConfigurations()
                .getByName("default")
                .getResolvedConfiguration();

        Set<ResolvedDependency> directDeps = resolvedConfiguration
                .getFirstLevelModuleDependencies();

        for (ResolvedDependency directDep : directDeps) {

            assert directDep.getModuleArtifacts().iterator().hasNext() : "Expected module artifacts";

            ArtifactSpec parent = new ArtifactSpec(
                    "compile",
                    directDep.getModule().getId().getGroup(),
                    directDep.getModule().getId().getName(),
                    directDep.getModule().getId().getVersion(),
                    directDep.getModuleArtifacts().iterator().next().getExtension(),
                    null,
                    null
            );
            Set<ArtifactSpec> artifactSpecs = resolvingHelper.resolveAll(new HashSet<>(Collections.singletonList(parent)));
            artifactSpecs.forEach(a -> declaredDependencies.add(parent, a));
        }

        tool.declaredDependencies(declaredDependencies);

        final Boolean bundleDependencies = getBundleDependencies();
        if (bundleDependencies != null) {
            this.tool.bundleDependencies(bundleDependencies);
        }

        this.tool.build(getBaseName(), getOutputDirectory());
    }

    @Input
    private String getPackaging() {
        return jarTask.getExtension();
    }

    @InputFile
    private File getProjectArtifactFile() {
        return jarTask.getArchivePath();
    }

    @Input
    @Optional
    private String getMainClassName() {
        Project project = getProject();
        SwarmExtension swarmExtension = getSwarmExtension();
        String mainClassName = swarmExtension.getMainClassName();

        if (mainClassName == null && project.getConvention().getPlugins().containsKey("application")) {
            ApplicationPluginConvention app = (ApplicationPluginConvention) project.getConvention().getPlugins().get("application");
            mainClassName = app.getMainClassName();
        }

        if (mainClassName != null && !mainClassName.equals("")) {
            getLogger().warn(
                    "\n------\n" +
                            "Custom main() usage is intended to be deprecated in a future release and is no longer supported, \n" +
                            "please refer to http://docs.wildfly-swarm.io for YAML configuration that replaces it." +
                            "\n------"
            );
        }

        return mainClassName;
    }

    private SwarmExtension getSwarmExtension() {
        return getProject().getExtensions().getByType(SwarmExtension.class);
    }

    @Input
    @Optional
    private Boolean getBundleDependencies() {
        return getSwarmExtension().getBundleDependencies();
    }

    @Input
    @Optional
    private Boolean getHollow() {
        return getSwarmExtension().getHollow();
    }

    @Input
    private boolean getExecutable() {
        return getSwarmExtension().getExecutable();
    }

    @Optional
    @InputFile
    private File getExecutableScript() {
        return getSwarmExtension().getExecutableScript();
    }

    @Input
    private Properties getPropertiesFromExtension() {
        return getSwarmExtension().getProperties();
    }

    @Input
    private Properties getPropertiesFromFile() {
        final Properties properties = new Properties();
        File propertiesFile = getPropertiesFile();

        if (propertiesFile != null) {
            try {
                properties.putAll(PropertiesUtil.loadProperties(propertiesFile));
            } catch (IOException e) {
                getLogger().error("Failed to load properties from " + propertiesFile, e);
            }
        }
        return properties;
    }

    @Optional
    @InputFile
    private File getPropertiesFile() {
        return getSwarmExtension().getPropertiesFile();
    }

    @InputFiles
    private List<File> getModuleDirs() {
        return getSwarmExtension().getModuleDirs();
    }

    @OutputFile
    private File getOutputFile() {
        return BuildTool.getOutputFile(getBaseName(), getOutputDirectory());
    }

    private String getBaseName() {
        return getProject().getName() + (this.getHollow() ? "-hollow" : "");
    }

    private Path getOutputDirectory() {
        return getProject().getBuildDir().toPath().resolve("libs");
    }

    /*private void addDependency(DeclaredDependencies declaredDependencies, final List<ArtifactSpec> explicitDependencies, final ResolvedArtifact gradleArtifact) {

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
    }*/
}
