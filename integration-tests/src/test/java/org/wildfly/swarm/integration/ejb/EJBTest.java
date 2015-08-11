package org.wildfly.swarm.integration.ejb;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.ejb.EJB;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.swarm.integration.base.AbstractWildFlySwarmTestCase;
import org.wildfly.swarm.jaxrs.JAXRSArchive;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author Ken Finnigan
 */
@RunWith(Arquillian.class)
public class EJBTest extends AbstractWildFlySwarmTestCase {

    @Deployment
    public static Archive createDeployment() throws Exception {
        JAXRSArchive deployment = ShrinkWrap.create(JAXRSArchive.class, "test.war");
        deployment.addResource(MyResource.class);
        deployment.addClass(GreeterEJB.class);
        deployment.addAllDependencies();
        return deployment;
    }

    @Test @RunAsClient
    public void testFromOutside() throws IOException {
        assertThat(fetch("http://localhost:8080/")).contains("Howdy from EJB");
    }

    @EJB
    private GreeterEJB greeter;

    @Test
    public void testFromInside() {
        assertThat( greeter.message() ).isEqualTo( "Howdy from EJB" );
    }

}
