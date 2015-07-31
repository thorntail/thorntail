package org.wildfly.swarm.arquillian.adapter;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.jboss.arquillian.container.spi.client.container.DeployableContainer;
import org.jboss.arquillian.container.spi.client.container.DeploymentException;
import org.jboss.arquillian.container.spi.client.container.LifecycleException;
import org.jboss.arquillian.container.spi.client.protocol.ProtocolDescription;
import org.jboss.arquillian.container.spi.client.protocol.metadata.HTTPContext;
import org.jboss.arquillian.container.spi.client.protocol.metadata.ProtocolMetaData;
import org.jboss.arquillian.container.spi.client.protocol.metadata.Servlet;
import org.jboss.arquillian.protocol.servlet.ServletMethodExecutor;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ArchivePath;
import org.jboss.shrinkwrap.api.Node;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.descriptor.api.Descriptor;
import org.jboss.shrinkwrap.resolver.api.maven.ConfigurableMavenResolverSystem;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.jboss.shrinkwrap.resolver.api.maven.MavenResolvedArtifact;
import org.jboss.shrinkwrap.resolver.api.maven.ScopeType;
import org.jboss.shrinkwrap.resolver.api.maven.coordinate.MavenCoordinate;
import org.jboss.shrinkwrap.resolver.api.maven.repository.MavenChecksumPolicy;
import org.jboss.shrinkwrap.resolver.api.maven.repository.MavenRemoteRepositories;
import org.jboss.shrinkwrap.resolver.api.maven.repository.MavenRemoteRepository;
import org.jboss.shrinkwrap.resolver.api.maven.repository.MavenUpdatePolicy;
import org.wildfly.swarm.tools.BuildTool;

/**
 * @author Bob McWhirter
 */
public class WildFlySwarmContainer implements DeployableContainer<WildFlySwarmContainerConfiguration> {

    private Process process;

    private LatchedBridge stdout;
    private IOBridge stderr;

    @Override
    public Class<WildFlySwarmContainerConfiguration> getConfigurationClass() {
        return WildFlySwarmContainerConfiguration.class;
    }

    @Override
    public void setup(WildFlySwarmContainerConfiguration config) {
    }

    @Override
    public void start() throws LifecycleException {
    }

    @Override
    public void stop() throws LifecycleException {
    }

    @Override
    public ProtocolDescription getDefaultProtocol() {
        return new ProtocolDescription("Servlet 3.0");
    }

    @Override
    public ProtocolMetaData deploy(Archive<?> archive) throws DeploymentException {

        BuildTool tool = new BuildTool();
        tool.projectArchive(archive);

        MavenRemoteRepository jbossPublic = MavenRemoteRepositories.createRemoteRepository("jboss-public-repository-group", "http://repository.jboss.org/nexus/content/groups/public/", "default");
        jbossPublic.setChecksumPolicy(MavenChecksumPolicy.CHECKSUM_POLICY_IGNORE);
        jbossPublic.setUpdatePolicy(MavenUpdatePolicy.UPDATE_POLICY_NEVER);

        ConfigurableMavenResolverSystem resolver = Maven.configureResolver()
                .withMavenCentralRepo(true)
                .withRemoteRepo(jbossPublic);

        tool.artifactResolvingHelper(new ArquillianArtifactResolvingHelper(resolver));

        MavenResolvedArtifact[] deps = resolver.loadPomFromFile("pom.xml").importDependencies(ScopeType.COMPILE).resolve().withTransitivity().asResolvedArtifact();

        for (MavenResolvedArtifact dep : deps) {
            MavenCoordinate coord = dep.getCoordinate();
            tool.dependency(dep.getScope().name(), coord.getGroupId(), coord.getArtifactId(), coord.getVersion(), coord.getPackaging().getExtension(), coord.getClassifier(), dep.asFile());
        }

        try {

            Path java = findJava();
            if (java == null) {
                throw new DeploymentException("Unable to locate `java` binary");
            }

            Archive wrapped = tool.build();
            File executable = File.createTempFile("arquillian", "-swarm.jar");
            wrapped.as(ZipExporter.class).exportTo(executable, true);

            List<String> cli = new ArrayList<>();

            cli.add(java.toString());
            cli.add("-jar");
            cli.add(executable.getAbsolutePath());

            this.process = Runtime.getRuntime().exec(cli.toArray(new String[cli.size()]));

            this.stdout = new LatchedBridge("out", process.getInputStream(), System.out);
            this.stderr = new IOBridge("err", process.getErrorStream(), System.err);

            new Thread(stdout).start();
            new Thread(stderr).start();

            ProtocolMetaData metaData = new ProtocolMetaData();
            HTTPContext context = new HTTPContext("localhost", 8080);
            context.add(new Servlet(ServletMethodExecutor.ARQUILLIAN_SERVLET_NAME, "/"));
            metaData.addContext(context);
            stdout.getLatch().await();
            return metaData;
        } catch (Exception e) {
            throw new DeploymentException(e.getMessage(), e);
        }
    }

    @Override
    public void undeploy(Archive<?> archive) throws DeploymentException {
        try {
            this.process.destroy();
            this.process.waitFor(10, TimeUnit.SECONDS);
            this.process.destroyForcibly();
            this.process.waitFor(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deploy(Descriptor descriptor) throws DeploymentException {
    }

    @Override
    public void undeploy(Descriptor descriptor) throws DeploymentException {
    }

    private Path findJava() {
        String javaHome = System.getProperty("java.home");
        if (javaHome == null) {
            return null;
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

        return null;
    }

    private static class IOBridge implements Runnable {

        private final String name;

        private final InputStream in;

        private final OutputStream out;

        public IOBridge(String name, InputStream in, OutputStream out) {
            this.name = name;
            this.in = in;
            this.out = out;
        }

        @Override
        public void run() {

            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            String line = null;
            try {
                while ((line = reader.readLine()) != null) {
                    processLine(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        protected void processLine(String line) throws IOException {
            out.write(line.getBytes());
            out.write('\n');
        }
    }

    private static class LatchedBridge extends IOBridge {

        private final CountDownLatch latch;

        public LatchedBridge(String name, InputStream in, OutputStream out) {
            super(name, in, out);
            this.latch = new CountDownLatch(1);
        }

        public CountDownLatch getLatch() {
            return this.latch;
        }

        @Override
        protected void processLine(String line) throws IOException {
            super.processLine(line);
            if (line.contains("WFLYSRV0010")) {
                this.latch.countDown();
            }
        }
    }
}
