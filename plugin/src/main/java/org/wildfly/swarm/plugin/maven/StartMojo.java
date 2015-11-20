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
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
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

/**
 * @author Bob McWhirter
 * @author Ken Finnigan
 */
@Mojo(name = "start",
        requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME,
        requiresDependencyCollection = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class StartMojo extends AbstractMojo {

    @Component
    protected MavenProject project;

    @Parameter(defaultValue = "${project.build.directory}")
    protected String projectBuildDir;

    @Parameter(alias = "mainClass")
    protected String mainClass;

    @Parameter(alias = "httpPort", defaultValue = "8080")
    private int httpPort;

    @Parameter(alias = "portOffset", defaultValue = "0")
    private int portOffset;

    @Parameter(alias = "bindAddress", defaultValue = "0.0.0.0")
    private String bindAddress;

    @Parameter(alias = "contextPath", defaultValue = "/")
    private String contextPath;

    @Parameter(alias = "properties")
    private Properties properties;

    @Parameter
    private Properties environment;

    @Parameter(alias = "environmentFile")
    private File environmentFile;

    @Parameter(alias = "stdoutFile")
    private File stdoutFile;

    @Parameter(alias = "stderrFile")
    private File stderrFile;

    boolean waitForProcess;

    private IOBridge stdout;

    private IOBridge stderr;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
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

        Process process = null;
        if (this.project.getPackaging().equals("war")) {
            process = executeWar();
        } else if (this.project.getPackaging().equals("jar")) {
            process = executeJar();
        } else {
            throw new MojoExecutionException("Unsupported packaging" + this.project.getPackaging());
        }

        getPluginContext().put("swarm-process", process);
        getPluginContext().put("swarm-io-stdout", this.stdout);
        getPluginContext().put("swarm-io-stderr", this.stderr);

        if (waitForProcess) {
            try {
                process.waitFor();
            } catch (InterruptedException e) {
            }
        }
    }

    protected Process executeWar() throws MojoFailureException {
        Path java = findJava();

        try {
            List<String> cli = new ArrayList<>();
            cli.add(java.toString());
            cli.add("-classpath");
            cli.add(dependencies(false));

            String finalName = this.project.getBuild().getFinalName();
            if (!finalName.endsWith(".war")) {
                finalName = finalName + ".war";
            }
            cli.add("-Dwildfly.swarm.app.path=" + Paths.get(this.projectBuildDir, finalName).toString());
            Properties runProps = runProperties();

            Enumeration<?> propNames = runProps.propertyNames();

            while (propNames.hasMoreElements()) {
                String name = (String) propNames.nextElement();
                cli.add("-D" + name + "=" + runProps.getProperty(name));
            }

            cli.add("-Dwildfly.swarm.context.path=" + this.contextPath);
            cli.add("org.wildfly.swarm.Swarm");

            Process process = Runtime.getRuntime().exec(cli.toArray(new String[0]), toStringArray(environment));

            CountDownLatch latch = new CountDownLatch(1);

            this.stdout = new IOBridge("stdout", latch, process.getInputStream(), System.out, this.stdoutFile);
            this.stderr = new IOBridge("stderr", latch, process.getErrorStream(), System.err, this.stderrFile);

            new Thread(stdout).start();
            new Thread(stderr).start();

            latch.await(2, TimeUnit.MINUTES);
            if ( ! process.isAlive() ) {
                throw new MojoFailureException( "Process failed to start" );
            }
            if ( this.stdout.getError() != null ) {
                throw new MojoFailureException( "Error starting process", this.stdout.getError() );
            }
            if ( this.stderr.getError() != null ) {
                throw new MojoFailureException( "Error starting process", this.stderr.getError() );
            }

            return process;
        } catch (IOException e) {
            throw new MojoFailureException("Error executing", e);
        } catch (InterruptedException e) {
            throw new MojoFailureException("Error waiting for deployment", e);
        }
    }

    protected Process executeJar() throws MojoFailureException {
        Path java = findJava();

        try {
            List<String> cli = new ArrayList<>();
            cli.add(java.toString());
            cli.add("-classpath");
            cli.add(dependencies(true));

            Properties runProps = runProperties();

            Enumeration<?> propNames = runProps.propertyNames();

            while (propNames.hasMoreElements()) {
                String name = (String) propNames.nextElement();
                cli.add("-D" + name + "=" + runProps.getProperty(name));
                System.err.println(name + " = " + runProps.getProperty(name));
            }

            cli.add("-Dwildfly.swarm.context.path=" + this.contextPath);
            if (this.mainClass != null) {
                cli.add(this.mainClass);
            } else {
                cli.add("org.wildfly.swarm.Swarm");
            }

            Process process = Runtime.getRuntime().exec(cli.toArray(new String[0]), toStringArray(environment));

            CountDownLatch latch = new CountDownLatch(1);

            this.stdout = new IOBridge("stdout", latch, process.getInputStream(), System.out, this.stdoutFile);
            this.stderr = new IOBridge("stderr", latch, process.getErrorStream(), System.err, this.stderrFile);

            new Thread(stdout).start();
            new Thread(stderr).start();

            latch.await(2, TimeUnit.MINUTES);
            if ( ! process.isAlive() ) {
                throw new MojoFailureException( "Process failed to start" );
            }
            if ( this.stdout.getError() != null ) {
                throw new MojoFailureException( "Error starting process", this.stdout.getError() );
            }
            if ( this.stderr.getError() != null ) {
                throw new MojoFailureException( "Error starting process", this.stderr.getError() );
            }

            return process;
        } catch (IOException e) {
            throw new MojoFailureException("Error executing", e);
        } catch (InterruptedException e) {
            throw new MojoFailureException("Error waiting for launch", e);
        }
    }

    private static String[] toStringArray(Properties env) {
        String[] esa = new String[env.size()];
        int i = 0;
        for (Entry<Object, Object> e : env.entrySet()) {
            esa[i++] = e.getKey().toString() + '=' + e.getValue().toString();
        }
        return esa;
    }

    Properties runProperties() {
        Properties props = new Properties();
        props.putAll(this.properties);

        Properties sysProps = System.getProperties();

        Set<String> names = sysProps.stringPropertyNames();
        for (String name : names) {
            if (name.startsWith("jboss") || name.startsWith("wildfly") || name.startsWith("swarm")) {
                props.put(name, sysProps.get(name));
            }
        }

        return props;
    }

    String dependencies(boolean includeProjectArtifact) {
        List<String> elements = new ArrayList<>();
        Set<Artifact> artifacts = this.project.getArtifacts();
        for (Artifact each : artifacts) {
            if (each.getGroupId().equals("org.jboss.logmanager") && each.getArtifactId().equals("jboss-logmanager")) {
                continue;
            }
            elements.add(each.getFile().toString());
        }

        if (includeProjectArtifact) {
            elements.add(this.project.getBuild().getOutputDirectory());
        }

        StringBuilder cp = new StringBuilder();

        Iterator<String> iter = elements.iterator();

        while (iter.hasNext()) {
            String element = iter.next();
            cp.append(element);
            if (iter.hasNext()) {
                cp.append(File.pathSeparatorChar);
            }
        }

        return cp.toString();
    }

    Path findBootstrap() throws MojoFailureException {

        Set<Artifact> artifacts = this.project.getArtifacts();

        for (Artifact each : artifacts) {
            if (each.getGroupId().equals("org.wildfly.swarm") && each.getArtifactId().equals("wildfly-swarm-bootstrap") && each.getType().equals("jar")) {
                return each.getFile().toPath();
            }
        }

        return null;
    }

    Path findJava() throws MojoFailureException {
        String javaHome = System.getProperty("java.home");
        if (javaHome == null) {
            throw new MojoFailureException("java.home not set, unable to locate java");
        }

        Path binDir = FileSystems.getDefault().getPath(javaHome, "bin");

        Path java = binDir.resolve("java.exe");
        if (java.toFile().exists()) {
            return java;
        }

        java = binDir.resolve("java");
        if (java.toFile().exists()) {
            return java;
        }

        throw new MojoFailureException("Unable to determine java binary");
    }

}

