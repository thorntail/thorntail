package org.wildfly.swarm.cdi.jaxrsapi;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;
import org.wildfly.swarm.jaxrs.JAXRSArchive;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author Ken Finnigan
 */
@RunWith(Arquillian.class)
@RunAsClient
public class JAXRSApiTest {

    @Drone
    WebDriver browser;

    @Deployment
    public static Archive createDeployment() {
        JAXRSArchive deployment = ShrinkWrap.create(JAXRSArchive.class);
        deployment.addPackage(MessageResource.class.getPackage());
        return deployment;
    }

    @Test
    public void syncCall() throws Exception {
        browser.navigate().to("http://localhost:8080/messages/sync");
        assertThat(browser.getPageSource()).contains(MessageResource.MESSAGE_PREFIX);
    }

    @Test
    public void asyncCall() throws Exception {
        browser.navigate().to("http://localhost:8080/messages/async");
        String pageSource = browser.getPageSource();
        assertThat(pageSource).contains(MessageResource.MESSAGE_PREFIX);
        assertThat(pageSource).doesNotContain("null");
    }

    @Test
    public void zoneCall() throws Exception {
        browser.navigate().to("http://localhost:8080/messages/asyncZone?zoneId=America/New_York");
        String pageSource = browser.getPageSource();
        assertThat(pageSource).contains(MessageResource.MESSAGE_PREFIX);
        assertThat(pageSource).doesNotContain("null");
    }

    @Test
    public void timeCall() throws Exception {
        browser.navigate().to("http://localhost:8080/messages/timeMessage");
        assertThat(browser.getPageSource()).contains(TimeResource.INTRO_MESSAGE);
    }

    @Test
    public void hello() throws Exception {
        browser.navigate().to("http://localhost:8080/messages/hello/james");
        assertThat(browser.getPageSource()).contains(TimeResource.MESSAGE_HELLO + "james");
    }
}
