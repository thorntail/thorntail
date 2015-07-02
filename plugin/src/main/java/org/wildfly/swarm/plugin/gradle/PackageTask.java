package org.wildfly.swarm.plugin.gradle;

import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.artifacts.ResolvedArtifact;
import org.gradle.api.artifacts.ResolvedDependency;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.bundling.Jar;
import org.wildfly.swarm.tools.BuildTool;

import java.util.Set;

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
        System.err.println("running task");

        Project project = getProject();

        SwarmExtension ext = (SwarmExtension) project.getExtensions().getByName("swarm");

        ConfigurationContainer configs = project.getConfigurations();
        Configuration compile = configs.getByName("compile");

        this.tool = new BuildTool(project.getBuildDir());
        this.tool.artifactResolvingHelper( new GradleArtifactResolvingHelper(project));

        Set<ResolvedDependency> deps = compile.getResolvedConfiguration().getFirstLevelModuleDependencies();
        for (ResolvedDependency each : deps) {
            walk(each);
        }

        this.tool.projectArtifact(project.getGroup().toString(), project.getName(), project.getVersion().toString(), jarTask.getExtension(), jarTask.getArchivePath());

        this.tool.mainClass( ext.getMainClass() );

        this.tool.build(project.getName());
    }

    private void walk(ResolvedDependency dep) {

        Set<ResolvedArtifact> artifacts = dep.getModuleArtifacts();
        for (ResolvedArtifact each : artifacts) {
            String[] parts = dep.getName().split(":");
            String groupId = parts[0];
            String artifactId = parts[1];
            String version = parts[2];
            this.tool.dependency("compile", groupId, artifactId, version, each.getExtension(), each.getClassifier(), each.getFile());
        }

        for (ResolvedDependency each : dep.getChildren()) {
            walk(each);
        }
    }
}
