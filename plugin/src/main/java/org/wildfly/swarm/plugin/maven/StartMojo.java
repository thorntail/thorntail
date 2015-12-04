/**
 * Copyright 2015 Red Hat, Inc, and individual contributors.
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
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.wildfly.swarm.tools.exec.SwarmExecutor;
import org.wildfly.swarm.tools.exec.SwarmProcess;

/**
 * @author Bob McWhirter
 * @author Ken Finnigan
 */
@Mojo(name = "start",
        requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME,
        requiresDependencyCollection = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class StartMojo extends AbstractMojo {

    @Component
    public MavenProject project;

    @Parameter(defaultValue = "${project.build.directory}")
    public String projectBuildDir;

    @Parameter(alias = "mainClass")
    public String mainClass;

    @Parameter(alias = "httpPort", defaultValue = "8080")
    public int httpPort;

    @Parameter(alias = "portOffset", defaultValue = "0")
    public int portOffset;

    @Parameter(alias = "bindAddress", defaultValue = "0.0.0.0")
    public String bindAddress;

    @Parameter(alias = "contextPath", defaultValue = "/")
    public String contextPath;

    @Parameter(alias = "properties")
    public Properties properties;

    @Parameter
    public Properties environment;

    @Parameter(alias = "environmentFile")
    public File environmentFile;

    @Parameter(alias = "stdoutFile")
    public File stdoutFile;

    @Parameter(alias = "stderrFile")
    public File stderrFile;

    @Parameter(alias = "useUberJar", defaultValue = "${wildfly-swarm.useUberJar}")
    public boolean useUberJar;

    @Parameter(alias = "debug")
    public Integer debugPort;

    boolean waitForProcess;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if ( this.contextPath == null ) {
            this.contextPath = "/";
        }
        if (this.properties == null) {
            this.properties = new Properties();
        }
        if (this.environment == null) {
            this.environment = new Properties();
        }
        if (environmentFile != null) {
            Properties ef = new Properties();
            try {
                Reader inStream = new FileReader(environmentFile);
                ef.load(inStream);
                inStream.close();
                this.environment.putAll(ef);
            } catch (IOException e) {
                getLog().error("env file not found or not parsable " + environmentFile);
            }
        }

        SwarmProcess process = null;

        if (this.useUberJar) {
            process = executeUberJar();
        } else if (this.project.getPackaging().equals("war")) {
            process = executeWar();
        } else if (this.project.getPackaging().equals("jar")) {
            process = executeJar();
        } else {
            throw new MojoExecutionException("Unsupported packaging: " + this.project.getPackaging());
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
            }
        }
    }

    protected SwarmProcess executeUberJar() throws MojoFailureException {
        getLog().info("Starting -swarm.jar");

        String finalName = this.project.getBuild().getFinalName();

        if (finalName.endsWith(".war") || finalName.endsWith(".jar")) {
            finalName = finalName.substring(0, finalName.length() - 4);
        }

        String uberJarName = finalName + "-swarm.jar";

        Path uberJar = Paths.get(this.projectBuildDir, uberJarName);

        try {
            SwarmProcess process = new SwarmExecutor()
                    .withDefaultSystemProperties()
                    .withDebug(debugPort)
                    .withProperties(this.properties)
                    .withEnvironment(this.environment)
                    .withWorkingDirectory(this.project.getBasedir().toPath())
                    .withStdoutFile(this.stdoutFile != null ? this.stdoutFile.toPath() : null)
                    .withStderrFile(this.stderrFile != null ? this.stderrFile.toPath() : null)
                    .withExecutableJar(uberJar)
                    .execute();

            process.awaitDeploy(2, TimeUnit.MINUTES);

            if (!process.isAlive()) {
                throw new MojoFailureException("Process failed to start");
            }
            if (process.getError() != null) {
                throw new MojoFailureException("Error starting process", process.getError());
            }

            return process;
        } catch (IOException e) {
            throw new MojoFailureException("unable to execute uberjar", e);
        } catch (InterruptedException e) {
            throw new MojoFailureException("Error waiting for deployment", e);
        }
    }

    protected SwarmProcess executeWar() throws MojoFailureException {
        getLog().info("Starting .war");

        SwarmExecutor executor = new SwarmExecutor()
                .withDebug(debugPort)
                .withDefaultSystemProperties()
                .withClassPathEntries(dependencies(false));

        try {

            String finalName = this.project.getBuild().getFinalName();
            if (!finalName.endsWith(".war")) {
                finalName = finalName + ".war";
            }
            executor.withProperty("wildfly.swarm.app.path", Paths.get(this.projectBuildDir, finalName).toString())
                    .withProperties(this.properties)
                    .withProperty("wildfly.swarm.context.path", this.contextPath)
                    .withEnvironment(this.environment)
                    .withWorkingDirectory(this.project.getBasedir().toPath())
                    .withDefaultMainClass();


            if (stdoutFile != null)
                executor.withStdoutFile(this.stdoutFile.toPath());
            if (stderrFile != null)
                executor.withStderrFile(this.stderrFile.toPath());


            SwarmProcess process = executor.execute();

            process.awaitDeploy(2, TimeUnit.MINUTES);

            if (!process.isAlive()) {
                throw new MojoFailureException("Process failed to start");
            }
            if (process.getError() != null) {
                throw new MojoFailureException("Error starting process", process.getError());
            }
            return process;
        } catch (IOException e) {
            throw new MojoFailureException("Error executing", e);
        } catch (InterruptedException e) {
            throw new MojoFailureException("Error waiting for deployment", e);
        }
    }

    protected SwarmProcess executeJar() throws MojoFailureException {
        getLog().info("Starting .jar");

        SwarmExecutor executor = new SwarmExecutor()
                .withDefaultSystemProperties()
                .withDebug(debugPort);

        try {
            executor.withClassPathEntries(dependencies(true))
                    .withProperties(this.properties)
                    .withProperty("wildfly.swarm.context.path", this.contextPath)
                    .withEnvironment(this.environment)
                    .withWorkingDirectory(this.project.getBasedir().toPath());

            if (this.mainClass != null) {
                executor.withMainClass(this.mainClass);
            } else {
                executor.withDefaultMainClass();
            }


            if (stdoutFile != null)
                executor.withStdoutFile(this.stdoutFile.toPath());
            if (stderrFile != null)
                executor.withStderrFile(this.stderrFile.toPath());

            SwarmProcess process = executor.execute();

            process.awaitDeploy(2, TimeUnit.MINUTES);

            if (!process.isAlive()) {
                throw new MojoFailureException("Process failed to start");
            }
            if (process.getError() != null) {
                throw new MojoFailureException("Error starting process", process.getError());
            }
            return process;
        } catch (IOException e) {
            throw new MojoFailureException("Error executing", e);
        } catch (InterruptedException e) {
            throw new MojoFailureException("Error waiting for launch", e);
        }
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
}

