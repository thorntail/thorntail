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

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.wildfly.swarm.bootstrap.util.BootstrapProperties;
import org.wildfly.swarm.tools.exec.SwarmExecutor;
import org.wildfly.swarm.tools.exec.SwarmProcess;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

        final SwarmProcess process;
        try {
            process = executor.withDebug(debugPort)
                    .withProperties(this.properties)
                    .withStdoutFile(this.stdoutFile != null ? this.stdoutFile.toPath() : null)
                    .withStderrFile(this.stderrFile != null ? this.stderrFile.toPath() : null)
                    .withEnvironment(this.environment)
                    .withWorkingDirectory(this.project.getBasedir().toPath())
                    .execute();

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
        if ( procs == null ) {
            procs = new ArrayList<>();
            getPluginContext().put("swarm-process", procs);
        }
        procs.add( process );

        if (waitForProcess) {
            try {
                process.waitFor();
            } catch (InterruptedException e) {
                throw new MojoExecutionException( "Error waiting for process to start", e );
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

        return new SwarmExecutor()
                .withModules(expandModules())
                .withClassPathEntries(dependencies(false))
                .withProperty(BootstrapProperties.APP_PATH,
                              Paths.get(this.projectBuildDir, finalName).toString())
                .withDefaultMainClass();
    }

    @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
    protected SwarmExecutor jarExecutor() throws MojoFailureException {
        getLog().info("Starting .jar");

        final SwarmExecutor executor = new SwarmExecutor()
                .withModules(expandModules())
                .withClassPathEntries(dependencies(true));

        if (this.mainClass != null) {
            executor.withMainClass(this.mainClass);
        } else {
            executor.withDefaultMainClass();
        }

        return executor;
    }

    List<Path> dependencies(boolean includeProjectArtifact) {

        List<Path> elements = new ArrayList<>();
        Set<Artifact> artifacts = this.project.getArtifacts();
        for (Artifact each : artifacts) {
            if (each.getGroupId().equals("org.jboss.logmanager") && each.getArtifactId().equals("jboss-logmanager")) {
                continue;
            }
            elements.add(each.getFile().toPath());
        }

        if (includeProjectArtifact) {
            elements.add(Paths.get(this.project.getBuild().getOutputDirectory()));
        }

        return elements;
    }

    List<Path> expandModules() {
        return Stream.of(this.additionalModules)
                .map(m -> Paths.get(this.project.getBuild().getOutputDirectory(), m))
                .collect(Collectors.toList());
    }
}

