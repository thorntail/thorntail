package org.wildfly.swarm.plugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

/**
 * @author Bob McWhirter
 * @author Ken Finnigan
 */
@Mojo(name = "run",
        requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME,
        requiresDependencyCollection = ResolutionScope.COMPILE_PLUS_RUNTIME)
@Execute(phase = LifecyclePhase.PACKAGE)
public class RunMojo extends AbstractMojo {

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

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (this.project.getPackaging().equals("war")) {
            executeWar();
        } else if (this.project.getPackaging().equals("jar")) {
            executeJar();
        }
    }

    protected void executeWar() throws MojoFailureException {
        Path java = findJava();

        try {
            List<String> cli = new ArrayList<>();
            cli.add(java.toString());
            cli.add("-classpath");
            cli.add(dependencies(false));
            cli.add("-Dwildfly.swarm.app.path=" + Paths.get(this.projectBuildDir, this.project.getBuild().getFinalName()).toString());
            cli.add("-Djboss.http.port=" + this.httpPort );
            cli.add("-Djboss.socket.binding.port-offset=" + this.portOffset );
            cli.add("-Djboss.bind.address=" + this.bindAddress );
            cli.add("org.wildfly.swarm.Swarm");

            Process process = Runtime.getRuntime().exec(cli.toArray(new String[cli.size()]));

            new Thread(new IOBridge(process.getInputStream(), System.out)).start();
            new Thread(new IOBridge(process.getErrorStream(), System.err)).start();

            process.waitFor();
        } catch (IOException e) {
            throw new MojoFailureException("Error executing", e);
        } catch (InterruptedException e) {
            // ignore;
        }

    }

    protected void executeJar() throws MojoFailureException {
        Path java = findJava();

        try {
            List<String> cli = new ArrayList<>();
            cli.add(java.toString());
            cli.add("-classpath");
            cli.add(dependencies(true));
            cli.add("-Djboss.http.port=" + this.httpPort );
            cli.add("-Djboss.socket.binding.port-offset=" + this.portOffset );
            cli.add("-Djboss.bind.address=" + this.bindAddress );
            if (this.mainClass != null) {
                cli.add(this.mainClass);
            } else {
                cli.add("org.wildfly.swarm.Swarm");
            }

            Process process = Runtime.getRuntime().exec(cli.toArray(new String[cli.size()]));

            new Thread(new IOBridge(process.getInputStream(), System.out)).start();
            new Thread(new IOBridge(process.getErrorStream(), System.err)).start();

            process.waitFor();
        } catch (IOException e) {
            throw new MojoFailureException("Error executing", e);
        } catch (InterruptedException e) {
            // ignore;
        }

    }

    String dependencies(boolean includeProjectArtifact) {
        List<String> elements = new ArrayList<>();
        Set<Artifact> artifacts = this.project.getArtifacts();
        for (Artifact each : artifacts) {
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


    private static class IOBridge implements Runnable {

        private final InputStream in;

        private final OutputStream out;

        public IOBridge(InputStream in, OutputStream out) {
            this.in = in;
            this.out = out;
        }

        @Override
        public void run() {

            byte[] buf = new byte[1024];
            int len = -1;

            try {
                while ((len = this.in.read(buf)) >= 0) {
                    out.write(buf, 0, len);
                }
            } catch (IOException e) {

            }

        }
    }

}

