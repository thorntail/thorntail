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

import java.util.Set;

import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.artifacts.ResolvedArtifact;
import org.gradle.api.artifacts.ResolvedDependency;
import org.gradle.api.plugins.ApplicationPluginConvention;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.bundling.Jar;
import org.wildfly.swarm.plugin.Util;
import org.wildfly.swarm.tools.BuildTool;

/**
 * @author Bob McWhirter
 */
public class PackageTask extends DefaultTask {

    private BuildTool tool;

    private Jar jarTask;

    public void jarTask(Jar jarTask) {
        this.jarTask = jarTask;
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

        this.tool = new BuildTool()
                .artifactResolvingHelper(new GradleArtifactResolvingHelper(project))
                .projectArtifact(project.getGroup().toString(), project.getName(), project.getVersion().toString(),
                                 jarTask.getExtension(), jarTask.getArchivePath())
                .mainClass(ext.getMainClassName())
                .properties(ext.getProperties())
                .properties(Util.filteredSystemProperties(ext.getProperties(), false));

        project.getConfigurations()
                .getByName("compile")
                .getResolvedConfiguration()
                .getFirstLevelModuleDependencies()
                .forEach(d -> walk(true, d));

        final Boolean bundleDependencies = ext.getBundleDependencies();
        if (bundleDependencies != null) {
            this.tool.bundleDependencies(bundleDependencies);
        }

        this.tool.build(project.getName(), project.getBuildDir().toPath().resolve("libs"));
    }

    private void walk(final boolean top, ResolvedDependency dep) {
        Set<ResolvedArtifact> artifacts = dep.getModuleArtifacts();
        for (ResolvedArtifact each : artifacts) {
            String[] parts = dep.getName().split(":");
            String groupId = parts[0];
            String artifactId = parts[1];
            String version = parts[2];
            this.tool.dependency("compile", groupId, artifactId, version, each.getExtension(),
                                 each.getClassifier(), each.getFile(), top);
        }

        dep.getChildren().forEach(d -> walk(false, d));
    }
}
