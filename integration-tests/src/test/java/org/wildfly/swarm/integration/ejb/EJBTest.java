package org.wildfly.swarm.integration.ejb;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import javax.ejb.EJB;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;
import org.wildfly.swarm.arquillian.adapter.ArtifactDependencies;
import org.wildfly.swarm.jaxrs.JAXRSArchive;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author Ken Finnigan
 */
@RunWith(Arquillian.class)
public class EJBTest {

    @ArquillianResource
    URL contextRoot;

    @Drone
    WebDriver browser;

    @EJB
    private GreeterEJB greeter;

    @Deployment
    public static Archive createDeployment() throws Exception {
        JAXRSArchive deployment = ShrinkWrap.create(JAXRSArchive.class, "ejb-test.war");
        deployment.addResource(MyResource.class);
        deployment.addClass(GreeterEJB.class);
        return deployment;
    }

    @ArtifactDependencies
    public static List<String> artifactDependencies() {
        return Arrays.asList(
                "org.wildfly.swarm:wildfly-swarm-ejb",
                "org.wildfly.swarm:wildfly-swarm-jaxrs"
        );
    }

    @Test
    @RunAsClient
    public void testFromOutside() throws IOException {
        browser.navigate().to(contextRoot);

        assertThat(browser.getPageSource()).contains("Howdy from EJB");
    }

    @Ignore
    @Test
    public void testFromInside() {
        assertThat(greeter.message()).isEqualTo("Howdy from EJB");
    }

}
