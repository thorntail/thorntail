package org.wildfly.swarm.swagger.webapp.unit;

import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.Node;
import org.junit.Test;
import org.wildfly.swarm.container.Container;
import org.wildfly.swarm.swagger.webapp.SwaggerWebAppFraction;

import static org.fest.assertions.Assertions.assertThat;



/**
 * @author Lance Ball
 */
public class SwaggerWebAppFractionTest {

    @Test
    public void testAddWebContentFromGAV() {
        SwaggerWebAppFraction fraction = new SwaggerWebAppFraction();
        fraction.addWebContent("org.wildfly.swarm:swagger-webapp-ui:war:" + Container.VERSION);
        assertArchive(fraction);
    }

    @Test
    public void testAddWebContentFromJar() {
        SwaggerWebAppFraction fraction = new SwaggerWebAppFraction();
        fraction.addWebContent("./test.jar");
        assertArchive(fraction);
    }

    @Test
    public void testAddWebContentFromDirectory() {
        SwaggerWebAppFraction fraction = new SwaggerWebAppFraction();
        fraction.addWebContent("./sut");
        Archive<?> archive = assertArchive(fraction);
        // make sure nested files are where they should be
        Node node = archive.get("/js/test.js");
        assertThat(node).isNotNull();
        node = archive.get("/js/lib/some-lib.js");
        assertThat(node).isNotNull();
    }

    private Archive<?> assertArchive(SwaggerWebAppFraction fraction) {
        Archive<?> archive = fraction.getWebContent();
        assertThat(archive).isNotNull();
        Node node = archive.get("/index.html");
        assertThat(node).isNotNull();
        return archive;
    }
}
