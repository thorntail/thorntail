package org.wildfly.swarm.management.console;

import java.io.IOException;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;
import org.wildfly.swarm.undertow.WARArchive;

import static org.fest.assertions.Assertions.assertThat;

/**
 * Created by ggastald on 01/07/16.
 */
@RunWith(Arquillian.class)
public class ManagementConsoleTest {

    @Drone
    WebDriver browser;

    @Deployment
    public static Archive createDeployment() throws Exception {
        return ShrinkWrap.create(WARArchive.class);
    }

    @Test
    @RunAsClient
    public void testHomePageShouldHaveCorrectTitle() throws IOException {
        browser.navigate().to("http://localhost:8080/console");
        assertThat(browser.getTitle()).isEqualToIgnoringCase("Management Interface");
    }

}