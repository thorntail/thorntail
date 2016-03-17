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

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.handler.ArtifactHandler;
import org.apache.maven.artifact.handler.DefaultArtifactHandler;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.wildfly.swarm.fractionlist.FractionList;
import org.wildfly.swarm.tools.BuildTool;
import org.wildfly.swarm.tools.FractionDescriptor;

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
        final Artifact primaryArtifact = this.project.getArtifact();
        final String finalName = this.project.getBuild().getFinalName();
        final String type = primaryArtifact.getType();
        final BuildTool tool = new BuildTool()
                .projectArtifact(primaryArtifact.getGroupId(),
                                 primaryArtifact.getArtifactId(),
                                 primaryArtifact.getBaseVersion(),
                                 type,
                                 primaryArtifact.getFile(),
                                 finalName.endsWith("." + type) ?
                                         finalName :
                                         String.format("%s.%s", finalName, type))
                .fractionList(FractionList.get())
                .properties(this.properties)
                .mainClass(this.mainClass)
                .bundleDependencies(this.bundleDependencies)
                .artifactResolvingHelper(mavenArtifactResolvingHelper());

        this.additionalFractions.stream()
                .map(f -> FractionDescriptor.fromGav(FractionList.get(), f))
                .map(FractionDescriptor::toArtifactSpec)
                .forEach(tool::fraction);

        this.project.getArtifacts()
                .forEach(dep -> tool.dependency(artifactToArtifactSpec(dep)));

        this.project.getResources()
                .forEach(r -> tool.resourceDirectory(r.getDirectory()));


        this.additionalModules.stream()
                .map(m -> new File(this.project.getBuild().getOutputDirectory(), m))
                .filter(File::exists)
                .map(File::getAbsolutePath)
                .forEach(tool::additionalModule);

        try {
            File jar = tool.build(finalName, Paths.get(this.projectBuildDir));

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

