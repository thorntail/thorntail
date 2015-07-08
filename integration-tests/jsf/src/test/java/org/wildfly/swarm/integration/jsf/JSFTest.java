package org.wildfly.swarm.integration.jsf;

import org.junit.After;
import org.junit.Test;
import org.wildfly.swarm.container.Container;
import org.wildfly.swarm.integration.base.AbstractWildFlySwarmTestCase;
import org.wildfly.swarm.undertow.WarDeployment;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author Ken Finnigan
 */
public class JSFTest extends AbstractWildFlySwarmTestCase {
    private Container container;

    //@Test
    //TODO Once https://issues.jboss.org/browse/WFLY-4889 is resolved, attempt to get the test working
    public void testSimple() throws Exception {
        container = newContainer();
        container.start();

        WarDeployment deployment = new WarDeployment(container);
        deployment.staticContent();
        container.deploy(deployment);

        String result = fetch("http://localhost:8080");
        assertThat(result).contains("This is static.");
    }

    @After
    public void shutdown() throws Exception {
        container.stop();
    }
}
