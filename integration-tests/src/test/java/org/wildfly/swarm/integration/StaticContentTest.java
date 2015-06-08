package org.wildfly.swarm.integration;

import java.io.InputStream;
import java.net.URL;

import org.junit.Test;
import org.wildfly.swarm.container.Container;
import org.wildfly.swarm.undertow.StaticDeployment;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author Bob McWhirter
 */
public class StaticContentTest extends AbstractWildFlySwarmTestCase {

    @Test
    public void testStaticContent() throws Exception {
        Container container = newContainer();
        container.start();
        StaticDeployment deployment = new StaticDeployment(container);
        container.deploy( deployment );
        String result = fetch( "http://localhost:8080/static-content.txt" );
        assertThat( result ).contains( "This is static." );
        container.stop();
    }
}
