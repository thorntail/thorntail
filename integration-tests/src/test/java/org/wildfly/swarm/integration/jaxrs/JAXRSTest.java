package org.wildfly.swarm.integration.jaxrs;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;
import org.wildfly.swarm.arquillian.adapter.ArtifactDependencies;
import org.wildfly.swarm.jaxrs.JAXRSArchive;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author Bob McWhirter
 */
@RunWith(Arquillian.class)
public class JAXRSTest {

    @ArquillianResource
    URL contextRoot;

    @Drone
    WebDriver browser;

    @Deployment
    public static Archive createDeployment() throws Exception {
        JAXRSArchive deployment = ShrinkWrap.create(JAXRSArchive.class);
        deployment.addResource(MyResource.class);
        deployment.staticContent();
        return deployment;
    }

    @ArtifactDependencies
    public static List<String> appDependencies() {
        return Arrays.asList(
                "org.wildfly.swarm:wildfly-swarm-jaxrs"
        );
    }

    @RunAsClient
    @Test
    public void testSimple() throws IOException {
        browser.navigate().to(contextRoot);
        assertThat(browser.getPageSource()).contains("This is index.html.");

        browser.navigate().to(contextRoot + "/static-content.txt");
        assertThat(browser.getPageSource()).contains("This is static.");
    }

}
