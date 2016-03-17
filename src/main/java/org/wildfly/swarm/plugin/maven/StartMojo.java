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
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.wildfly.swarm.bootstrap.util.BootstrapProperties;
import org.wildfly.swarm.fractionlist.FractionList;
import org.wildfly.swarm.tools.ArtifactSpec;
import org.wildfly.swarm.tools.DependencyManager;
import org.wildfly.swarm.tools.FractionDescriptor;
import org.wildfly.swarm.tools.FractionUsageAnalyzer;
import org.wildfly.swarm.tools.exec.SwarmExecutor;
import org.wildfly.swarm.tools.exec.SwarmProcess;

/**
 * @author Bob McWhirter
 * @author Ken Finnigan
 */
@Mojo(name = "start",
        requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME,
        requiresDependencyCollection = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class StartMojo extends AbstractSwarmMojo {

    @Parameter(alias = "stdoutFile")
    public File stdoutFile;

    @Parameter(alias = "stderrFile")
    public File stderrFile;

    @Parameter(alias = "useUberJar", defaultValue = "${wildfly-swarm.useUberJar}")
    public boolean useUberJar;

    @Parameter(alias = "debug", property = BootstrapProperties.DEBUG_PORT)
    public Integer debugPort;

    @Parameter(alias = "jvmArguments" )
    public List<String> jvmArguments = new ArrayList<>();

    boolean waitForProcess;

    @SuppressWarnings("unchecked")
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        initProperties(true);
        initEnvironment();

        final SwarmExecutor executor;

        if (this.useUberJar) {
            executor = uberJarExecutor();
        } else if (this.project.getPackaging().equals("war")) {
            executor = warExecutor();
        } else if (this.project.getPackaging().equals("jar")) {
            executor = jarExecutor();
        } else {
            throw new MojoExecutionException("Unsupported packaging: " + this.project.getPackaging());
        }

        executor.withJVMArguments( this.jvmArguments );

        final SwarmProcess process;
        try {
            process = executor.withDebug(debugPort)
                    .withProperties(this.properties)
                    .withStdoutFile(this.stdoutFile != null ? this.stdoutFile.toPath() : null)
                    .withStderrFile(this.stderrFile != null ? this.stderrFile.toPath() : null)
                    .withEnvironment(this.environment)
                    .withWorkingDirectory(this.project.getBasedir().toPath())
                    .execute();

            Runtime.getRuntime().addShutdownHook( new Thread(()->{
                try {
                    process.stop( 10, TimeUnit.SECONDS );
                } catch (InterruptedException e) {
                }
            }));

            process.awaitDeploy(2, TimeUnit.MINUTES);

            if (!process.isAlive()) {
                throw new MojoFailureException("Process failed to start");
            }
            if (process.getError() != null) {
                throw new MojoFailureException("Error starting process", process.getError());
            }

        } catch (IOException e) {
            throw new MojoFailureException("unable to execute", e);
        } catch (InterruptedException e) {
            throw new MojoFailureException("Error waiting for deployment", e);
        }

        List<SwarmProcess> procs = (List<SwarmProcess>) getPluginContext().get("swarm-process");
        if (procs == null) {
            procs = new ArrayList<>();
            getPluginContext().put("swarm-process", procs);
        }
        procs.add(process);

        if (waitForProcess) {
            try {
                process.waitFor();
            } catch (InterruptedException e) {
                throw new MojoExecutionException("Error waiting for process to start", e);
            }
        }
    }

    @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
    protected SwarmExecutor uberJarExecutor() throws MojoFailureException {
        getLog().info("Starting -swarm.jar");

        String finalName = this.project.getBuild().getFinalName();

        if (finalName.endsWith(".war") || finalName.endsWith(".jar")) {
            finalName = finalName.substring(0, finalName.length() - 4);
        }

        return new SwarmExecutor()
                .withExecutableJar(Paths.get(this.projectBuildDir, finalName + "-swarm.jar"));
    }

    @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
    protected SwarmExecutor warExecutor() throws MojoFailureException {
        getLog().info("Starting .war");

        String finalName = this.project.getBuild().getFinalName();
        if (!finalName.endsWith(".war")) {
            finalName = finalName + ".war";
        }
        final Path warFile = Paths.get(this.projectBuildDir, finalName);

        return new SwarmExecutor()
                .withModules(expandModules())
                .withClassPathEntries(dependencies(warFile, false))
                .withProperty(BootstrapProperties.APP_PATH,
                              warFile.toString())
                .withDefaultMainClass();
    }

    @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
    protected SwarmExecutor jarExecutor() throws MojoFailureException {
        getLog().info("Starting .jar");

        final String finalName = this.project.getBuild().getFinalName();

        final SwarmExecutor executor = new SwarmExecutor()
                .withModules(expandModules())
                .withProperty(BootstrapProperties.APP_ARTIFACT,
                              finalName.endsWith(".jar") ? finalName : finalName + ".jar")
                .withClassPathEntries(dependencies(Paths.get(this.project.getBuild().getOutputDirectory()), true));

        if (this.mainClass != null) {
            executor.withMainClass(this.mainClass);
        } else {
            executor.withDefaultMainClass();
        }

        return executor;
    }

    List<Path> findNeededFractions(final Set<Artifact> existingDeps, final Path source) throws MojoFailureException {
        getLog().info("No WildFly Swarm dependencies found - scanning for needed fractions");

        final Set<FractionDescriptor> fractions;
        try {
            fractions = new FractionUsageAnalyzer(FractionList.get(), source)
                    .detectNeededFractions();
        } catch (IOException e) {
            throw new MojoFailureException("failed to scan for fractions", e);
        }

        getLog().info("Detected fractions: " + String.join(", ", fractions.stream()
                .map(FractionDescriptor::av)
                .sorted()
                .collect(Collectors.toList())));

        fractions.addAll(this.additionalFractions.stream()
                         .map(f -> FractionDescriptor.fromGav(FractionList.get(), f))
                         .collect(Collectors.toSet()));

        final Set<FractionDescriptor> allFractions = new HashSet<>(fractions);
        allFractions.addAll(fractions.stream()
                                    .flatMap(f -> f.getDependencies().stream())
                                    .collect(Collectors.toSet()));


        getLog().info("Using fractions: " +
                              String.join(", ", allFractions.stream()
                                      .map(FractionDescriptor::gavOrAv)
                                      .sorted()
                                      .collect(Collectors.toList())));

        final Set<ArtifactSpec> specs = new HashSet<>();
        specs.addAll(existingDeps.stream()
                             .map(this::artifactToArtifactSpec)
                             .collect(Collectors.toList()));
        specs.addAll(fractions.stream()
                             .map(FractionDescriptor::toArtifactSpec)
                             .collect(Collectors.toList()));
        try {
            return mavenArtifactResolvingHelper().resolveAll(specs).stream()
                    .map(s -> s.file.toPath())
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new MojoFailureException("failed to resolve fraction dependencies", e);
        }
    }

    List<Path> dependencies(final Path archiveContent, final boolean includeProjectArtifact) throws MojoFailureException {
        final List<Path> elements = new ArrayList<>();
        final Set<Artifact> artifacts = this.project.getArtifacts();
        boolean hasSwarmDeps = false;
        for (Artifact each : artifacts) {
            if (each.getGroupId().equals(DependencyManager.WILDFLY_SWARM_GROUP_ID)
                    && each.getArtifactId().equals(DependencyManager.WILDFLY_SWARM_BOOTSTRAP_ARTIFACT_ID)) {
                hasSwarmDeps = true;
            }
            if (each.getGroupId().equals("org.jboss.logmanager")
                    && each.getArtifactId().equals("jboss-logmanager")) {
                continue;
            }
            elements.add(each.getFile().toPath());
        }

        if (includeProjectArtifact) {
            elements.add(Paths.get(this.project.getBuild().getOutputDirectory()));
        }

        if (!hasSwarmDeps) {
            elements.addAll(findNeededFractions(artifacts, archiveContent));
        }

        return elements;
    }

    List<Path> expandModules() {
        return this.additionalModules.stream()
                .map(m -> Paths.get(this.project.getBuild().getOutputDirectory(), m))
                .collect(Collectors.toList());
    }
}