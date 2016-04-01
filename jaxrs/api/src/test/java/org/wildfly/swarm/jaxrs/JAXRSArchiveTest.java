package org.wildfly.swarm.jaxrs;

import org.jboss.shrinkwrap.api.Node;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.wildfly.swarm.spi.api.JARArchive;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author Bob McWhirter
 */
public class JAXRSArchiveTest {

    public static final String PATH = "WEB-INF/classes/org/wildfly/swarm/generated/WildFlySwarmDefaultJAXRSApplication.class";

    @Test
    public void testApplicationPathAnnotation_None() {
        JAXRSArchive archive = ShrinkWrap.create( JAXRSArchive.class );

        Node generated = archive.get(PATH);
        Asset asset = generated.getAsset();

        assertThat( generated ).isNotNull();
        assertThat( asset ).isNotNull();
    }

    @Test
    public void testApplicationPathAnnotation_DirectlyInArchive() {
        JAXRSArchive archive = ShrinkWrap.create( JAXRSArchive.class );

        archive.addClass( MySampleApplication.class );

        Node generated = archive.get(PATH);
        assertThat( generated ).isNull();
    }

    @Test
    public void testApplicationPathAnnotation_InWebInfLibArchive() {

        JAXRSArchive archive = ShrinkWrap.create( JAXRSArchive.class );
        JavaArchive subArchive = ShrinkWrap.create(JavaArchive.class, "mysubarchive.jar");

        subArchive.addClass( MySampleApplication.class );
        archive.addAsLibrary( subArchive );

        Node generated = archive.get(PATH);
        assertThat( generated ).isNull();
    }

}
