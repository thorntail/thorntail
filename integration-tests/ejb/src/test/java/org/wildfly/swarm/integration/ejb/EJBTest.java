package org.wildfly.swarm.integration.ejb;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.junit.Test;
import org.wildfly.swarm.container.Container;
import org.wildfly.swarm.integration.base.AbstractWildFlySwarmTestCase;
import org.wildfly.swarm.jaxrs.JAXRSArchive;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author Ken Finnigan
 */
public class EJBTest extends AbstractWildFlySwarmTestCase {

    @Test
    public void testSimple() throws Exception {
        Container container = newContainer();
        container.start();

        JAXRSArchive deployment = ShrinkWrap.create( JAXRSArchive.class );
        deployment.addResource(MyResource.class);
        deployment.addClass(GreeterEJB.class);
        container.deploy(deployment);

        assertThat(fetch("http://localhost:8080/")).contains("Howdy from EJB");
        container.stop();
    }
}
