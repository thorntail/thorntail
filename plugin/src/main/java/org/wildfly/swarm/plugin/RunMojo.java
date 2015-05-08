package org.wildfly.swarm.plugin;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Enumeration;

/**
 * @author Bob McWhirter
 * @author Ken Finnigan
 */
@Mojo(name = "run")
@Execute(phase = LifecyclePhase.PACKAGE)
public class RunMojo extends AbstractMojo {

    @Component
    protected MavenProject project;

    @Parameter(defaultValue = "${project.build.directory}")
    protected String projectBuildDir;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        Path java = findJava();
        Path jar = findJar();

        try {
            Process process = Runtime.getRuntime().exec(new String[]{
                    java.toString(),
                    "-jar",
                    jar.toString()

            });

            new Thread( new IOBridge( process.getInputStream(), System.out ) ).start();
            new Thread( new IOBridge( process.getErrorStream(), System.err ) ).start();
            
            process.waitFor();
        } catch (IOException e) {
            throw new MojoFailureException("Error executing", e);
        } catch (InterruptedException e) {
            // ignore;
        }
    }

    Path findJar() throws MojoFailureException {
        Path jar = FileSystems.getDefault().getPath(this.projectBuildDir, this.project.getArtifactId() + "-" + this.project.getVersion() + "-swarm.jar");

        if (jar.toFile().exists()) {
            return jar;
        }

        throw new MojoFailureException("WildFly Swarm artifact does not exist");
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
                while ( ( len = this.in.read(buf) )>=0) {
                    out.write( buf, 0, len );
                }
            } catch (IOException e) {

            }

        }
    }

}

