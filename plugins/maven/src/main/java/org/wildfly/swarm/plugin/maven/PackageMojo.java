/**
 * Copyright 2015-2017 Red Hat, Inc, and individual contributors.
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Set;

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
import org.wildfly.swarm.fractions.FractionDescriptor;
import org.wildfly.swarm.fractions.FractionList;
import org.wildfly.swarm.spi.meta.SimpleLogger;
import org.wildfly.swarm.tools.ArtifactSpec;
import org.wildfly.swarm.tools.BuildTool;
import org.wildfly.swarm.tools.DeclaredDependencies;

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

    static final String UBERJAR_SUFFIX = "thorntail";

    static final String HOLLOWJAR_SUFFIX = "hollow" + "-" + UBERJAR_SUFFIX;

    @Parameter(alias = "bundleDependencies", defaultValue = "true", property = "thorntail.bundleDependencies")
    protected boolean bundleDependencies;

    @Parameter(alias = "filterWebinfLib", defaultValue = "true", property = "thorntail.filterWebinfLib")
    protected boolean filterWebinfLib;

    /**
     * Make a fully executable jar for *nix machines by prepending a launch script to the jar.
     */
    @Parameter(alias = "executable", defaultValue = "false", property = "thorntail.executable")
    protected boolean executable;

    /**
     * A custom script for *nix machines by prepending a launch script to the jar. Added only when executable = true
     */
    @Parameter(alias = "executableScript")
    protected File executableScript;

    @Parameter(alias = "hollow", defaultValue = "false", property = "thorntail.hollow")
    protected boolean hollow;

    @Parameter(property = "finalName")
    public String finalName;

    /**
     * Flag to skip packaging entirely.
     */
    @Parameter(alias = "skip", defaultValue = "false", property = "thorntail.package.skip")
    protected boolean skip;

    @Parameter(alias = "uberjarResources")
    protected String uberjarResources;

    protected File divineFile() {
        if (this.project.getArtifact().getFile() != null) {
            return this.project.getArtifact().getFile();
        }

        String finalName = this.project.getBuild().getFinalName();

        Path candidate = Paths.get(this.projectBuildDir, finalName + "." + this.project.getPackaging());

        if (Files.exists(candidate)) {
            return candidate.toFile();
        }
        return null;
    }


    @SuppressWarnings("deprecation")
    @Override
    public void executeSpecific() throws MojoExecutionException, MojoFailureException {

        if (this.skip) {
            getLog().info("Skipping packaging");
            return;
        }
        if (this.project.getPackaging().equals("pom")) {
            getLog().info("Not processing project with pom packaging");
            return;
        }
        initProperties(false);
        final Artifact primaryArtifact = this.project.getArtifact();
        final String finalName = this.project.getBuild().getFinalName();
        final String type = primaryArtifact.getType();

        final File primaryArtifactFile = divineFile();

        if (primaryArtifactFile == null) {
            throw new MojoExecutionException("Cannot package without a primary artifact; please `mvn package` prior to invoking thorntail:package from the command-line");
        }

        final DeclaredDependencies declaredDependencies = new DeclaredDependencies();

        final BuildTool tool = new BuildTool(mavenArtifactResolvingHelper())
                .projectArtifact(primaryArtifact.getGroupId(),
                        primaryArtifact.getArtifactId(),
                        primaryArtifact.getBaseVersion(),
                        type,
                        primaryArtifactFile,
                        finalName.endsWith("." + type) ?
                                finalName :
                                String.format("%s.%s", finalName, type))
                .properties(this.properties)
                .mainClass(this.mainClass)
                .bundleDependencies(this.bundleDependencies)
                .filterWebinfLib(this.filterWebinfLib)
                .executable(executable)
                .executableScript(executableScript)
                .fractionDetectionMode(fractionDetectMode)
                .hollow(hollow)
                .logger(new SimpleLogger() {
                    @Override
                    public void debug(String msg) {
                        getLog().debug(msg);
                    }

                    @Override
                    public void info(String msg) {
                        getLog().info(msg);
                    }

                    @Override
                    public void error(String msg) {
                        getLog().error(msg);
                    }

                    @Override
                    public void error(String msg, Throwable t) {
                        getLog().error(msg, t);
                    }
                });

        this.fractions.forEach(f -> {
            if (f.startsWith(EXCLUDE_PREFIX)) {
                tool.excludeFraction(ArtifactSpec.fromFractionDescriptor(FractionDescriptor.fromGav(FractionList.get(), f.substring(1))));
            } else {
                tool.fraction(ArtifactSpec.fromFractionDescriptor(FractionDescriptor.fromGav(FractionList.get(), f)));
            }
        });

        Map<ArtifactSpec, Set<ArtifactSpec>> buckets = createBuckets(this.project.getArtifacts(), this.project.getDependencies());

        for (ArtifactSpec directDep : buckets.keySet()) {

            if (!(directDep.scope.equals("compile") || directDep.scope.equals("runtime"))) {
                continue; // ignore anything but compile and runtime
            }

            Set<ArtifactSpec> transientDeps = buckets.get(directDep);
            if (transientDeps.isEmpty()) {
                declaredDependencies.add(directDep);
            } else {
                for (ArtifactSpec transientDep : transientDeps) {
                    declaredDependencies.add(directDep, transientDep);
                }
            }
        }

        tool.declaredDependencies(declaredDependencies);

        this.project.getResources()
                .forEach(r -> tool.resourceDirectory(r.getDirectory()));

        Path uberjarResourcesDir = null;
        if (this.uberjarResources == null) {
            uberjarResourcesDir = Paths.get(this.project.getBasedir().toString()).resolve("src").resolve("main").resolve("uberjar");
        } else {
            uberjarResourcesDir = Paths.get(this.uberjarResources);
        }
        tool.uberjarResourcesDirectory(uberjarResourcesDir);

        this.additionalModules.stream()
                .map(m -> new File(this.project.getBuild().getOutputDirectory(), m))
                .filter(File::exists)
                .map(File::getAbsolutePath)
                .forEach(tool::additionalModule);

        try {
            String jarFinalName;
            if (this.finalName != null) {
                jarFinalName = this.finalName;
            } else {
                jarFinalName = finalName + "-" + (this.hollow ? HOLLOWJAR_SUFFIX : UBERJAR_SUFFIX);
            }
            jarFinalName += JAR_FILE_EXTENSION;
            File jar = tool.build(jarFinalName, Paths.get(this.projectBuildDir));
            ArtifactHandler handler = new DefaultArtifactHandler(JAR);
            Artifact swarmJarArtifact = new DefaultArtifact(
                    primaryArtifact.getGroupId(),
                    primaryArtifact.getArtifactId(),
                    primaryArtifact.getBaseVersion(),
                    primaryArtifact.getScope(),
                    JAR,
                    (this.hollow ? HOLLOWJAR_SUFFIX : UBERJAR_SUFFIX),
                    handler
            );

            swarmJarArtifact.setFile(jar);
            this.project.addAttachedArtifact(swarmJarArtifact);

            if (this.project.getPackaging().equals(WAR)) {
                tool.repackageWar(primaryArtifactFile);
            }
        } catch (Exception e) {
            throw new MojoFailureException("Unable to create " + UBERJAR_SUFFIX + JAR_FILE_EXTENSION, e);
        }
    }

}

