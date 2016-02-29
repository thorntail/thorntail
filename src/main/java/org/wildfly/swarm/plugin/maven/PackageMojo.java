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
package org.wildfly.swarm.plugin.maven;

import java.io.File;
import java.nio.file.Paths;
import java.util.List;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.handler.ArtifactHandler;
import org.apache.maven.artifact.handler.DefaultArtifactHandler;
import org.apache.maven.model.Resource;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.wildfly.swarm.tools.BuildTool;

/**
 * @author Bob McWhirter
 * @author Ken Finnigan
 */
@Mojo(
        name = "package",
        defaultPhase = LifecyclePhase.PACKAGE,
        requiresDependencyCollection = ResolutionScope.COMPILE_PLUS_RUNTIME,
        requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME
)
public class PackageMojo extends AbstractSwarmMojo {

    @Parameter(alias = "bundleDependencies", defaultValue = "true")
    protected boolean bundleDependencies;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        initProperties(false);

        final BuildTool tool = new BuildTool();

        tool.projectArtifact(
                this.project.getArtifact().getGroupId(),
                this.project.getArtifact().getArtifactId(),
                this.project.getArtifact().getBaseVersion(),
                this.project.getArtifact().getType(),
                this.project.getArtifact().getFile());


        this.project.getArtifacts()
                .forEach(dep -> tool.dependency(artifactToArtifactSpec(dep)));

        List<Resource> resources = this.project.getResources();
        for (Resource each : resources) {
            tool.resourceDirectory(each.getDirectory());
        }

        for (String additionalModule : additionalModules) {
            File source = new File(this.project.getBuild().getOutputDirectory(), additionalModule);
            if (source.exists()) {
                tool.additionalModule(source.getAbsolutePath());
            }
        }

        tool.properties(this.properties)
                .mainClass(this.mainClass)
                .bundleDependencies(this.bundleDependencies)
                .artifactResolvingHelper(mavenArtifactResolvingHelper());

        try {
            File jar = tool.build(this.project.getBuild().getFinalName(), Paths.get(this.projectBuildDir));

            Artifact primaryArtifact = this.project.getArtifact();

            ArtifactHandler handler = new DefaultArtifactHandler("jar");
            Artifact swarmJarArtifact = new DefaultArtifact(
                    primaryArtifact.getGroupId(),
                    primaryArtifact.getArtifactId(),
                    primaryArtifact.getBaseVersion(),
                    primaryArtifact.getScope(),
                    "jar",
                    "swarm",
                    handler
            );

            swarmJarArtifact.setFile(jar);

            this.project.addAttachedArtifact(swarmJarArtifact);
        } catch (Exception e) {
            throw new MojoFailureException("Unable to create -swarm.jar", e);
        }
    }

}

