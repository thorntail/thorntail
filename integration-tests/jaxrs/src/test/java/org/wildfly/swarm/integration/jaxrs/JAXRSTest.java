package org.wildfly.swarm.integration.jaxrs;

import java.util.concurrent.CountDownLatch;

import org.junit.Test;
import org.wildfly.swarm.container.Container;
import org.wildfly.swarm.container.WarDeployment;
import org.wildfly.swarm.integration.base.AbstractWildFlySwarmTestCase;
import org.wildfly.swarm.jaxrs.JAXRSDeployment;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author Bob McWhirter
 */
public class JAXRSTest extends AbstractWildFlySwarmTestCase {

    @Test
    public void testSimple() throws Exception {
        Container container = newContainer();
        container.start();

        JAXRSDeployment deployment = new JAXRSDeployment(container);
        deployment.addResource(MyResource.class);
        container.deploy(deployment);

        assertThat(fetch("http://localhost:8080/")).contains("Howdy at");
        container.stop();
    }

    @Test
    public void testSimpleWithStatic() throws Exception {
        Container container = newContainer();
        container.start();

        JAXRSDeployment deployment = new JAXRSDeployment(container);

        deployment.staticContent();
        deployment.addResource(MyResource.class);

        container.deploy(deployment);

        //CountDownLatch latch = new CountDownLatch(1);
        //latch.await();

        assertThat(fetch("http://localhost:8080/")).contains("Howdy at");
        assertThat(fetch("http://localhost:8080/static-content.txt")).contains("This is static.");

        container.stop();
    }
}
