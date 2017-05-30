package org.wildfly.swarm.jaxrs.runtime;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.jboss.shrinkwrap.api.Node;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.wildfly.swarm.jaxrs.JAXRSArchive;
import org.wildfly.swarm.jaxrs.MyResource;
import org.wildfly.swarm.jaxrs.MySampleApplication;

import static org.fest.assertions.Assertions.assertThat;

public class DefaultApplicationDeploymentProcessorTest {

    private static final String PATH = "WEB-INF/classes/org/wildfly/swarm/generated/WildFlySwarmDefaultJAXRSApplication.class";

    @Test
    public void testApplicationPathAnnotation_None() throws Exception {
        JAXRSArchive archive = ShrinkWrap.create(JAXRSArchive.class);
        DefaultApplicationDeploymentProcessor processor = new DefaultApplicationDeploymentProcessor(archive);

        processor.process();

        Node generated = archive.get(PATH);
        Asset asset = generated.getAsset();

        assertThat(generated).isNotNull();
        assertThat(asset).isNotNull();
    }

    @Test
    public void testApplicationPathAnnotation_None_And_ChangeThePath() throws Exception {
        String applicationPath = "/api";

        JAXRSArchive archive = ShrinkWrap.create(JAXRSArchive.class);
        DefaultApplicationDeploymentProcessor processor = new DefaultApplicationDeploymentProcessor(archive);
        processor.applicationPath.set(applicationPath);

        processor.process();

        try (InputStream in = archive.get(PATH).getAsset().openStream()) {
            ClassReader reader = new ClassReader(in);
            ClassNode node = new ClassNode();

            reader.accept(node, 0);
            List<AnnotationNode> visibleAnnotations = node.visibleAnnotations;

            assertThat(visibleAnnotations.size()).isEqualTo(1);
            assertThat(visibleAnnotations.get(0).values).contains(applicationPath);
        } catch (IOException ignored) {
        }
    }

    @Test
    public void testApplicationPathAnnotation_DirectlyInArchive() throws Exception {
        JAXRSArchive archive = ShrinkWrap.create(JAXRSArchive.class);
        archive.addClass(MySampleApplication.class);
        DefaultApplicationDeploymentProcessor processor = new DefaultApplicationDeploymentProcessor(archive);

        processor.process();

        Node generated = archive.get(PATH);
        assertThat(generated).isNull();
    }

    @Test
    public void testApplicationPathAnnotation_InWebInfLibArchive() throws Exception {
        JavaArchive subArchive = ShrinkWrap.create(JavaArchive.class, "mysubarchive.jar");
        subArchive.addClass(MySampleApplication.class);
        JAXRSArchive archive = ShrinkWrap.create(JAXRSArchive.class);
        archive.addAsLibrary(subArchive);
        DefaultApplicationDeploymentProcessor processor = new DefaultApplicationDeploymentProcessor(archive);

        processor.process();

        Node generated = archive.get(PATH);
        assertThat(generated).isNull();
    }

    @Test
    public void testWebXmlApplicationServletMappingPresent() throws Exception {
        JAXRSArchive archive = ShrinkWrap.create(JAXRSArchive.class);
        archive.addClass(MyResource.class);
        archive.setWebXML(new StringAsset(
                "<web-app><servlet-mapping><servlet-name>Faces Servlet</servlet-name><url-pattern>*.jsf</url-pattern></servlet-mapping><servlet-mapping><servlet-name>javax.ws.rs.core.Application</servlet-name><url-pattern>/foo/*</url-pattern></servlet-mapping></web-app>"));
        DefaultApplicationDeploymentProcessor processor = new DefaultApplicationDeploymentProcessor(archive);

        processor.process();

        Node generated = archive.get(PATH);
        assertThat(generated).isNull();
    }

    @Test
    public void testWebXmlApplicationServletMappingAbsent() throws Exception {
        JAXRSArchive archive = ShrinkWrap.create(JAXRSArchive.class);
        archive.addClass(MyResource.class);
        archive.setWebXML(new StringAsset("<web-app><display-name>Foo</display-name></web-app>"));
        DefaultApplicationDeploymentProcessor processor = new DefaultApplicationDeploymentProcessor(archive);

        processor.process();

        Node generated = archive.get(PATH);
        assertThat(generated).isNotNull();
    }

    @Test
    public void testMalformedWebXmlApplicationServletMappingAbsent() throws Exception {
        JAXRSArchive archive = ShrinkWrap.create(JAXRSArchive.class);
        archive.addClass(MyResource.class);
        archive.setWebXML(new StringAsset("blablabla"));
        DefaultApplicationDeploymentProcessor processor = new DefaultApplicationDeploymentProcessor(archive);

        processor.process();

        Node generated = archive.get(PATH);
        assertThat(generated).isNotNull();
    }

}