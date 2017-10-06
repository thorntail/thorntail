package org.wildfly.swarm.howto.datasources;

import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import static org.junit.Assert.*;

/**
 * @author Bob McWhirter
 * @author Ken Finnigan
 */
@RunWith(Arquillian.class)
public class DatasourcesIT {

    @Drone
    WebDriver browser;

    @Test
    public void testIt() throws Exception {
        browser.navigate().to("http://localhost:8080/");
        assertEquals("Found the datasource", browser.findElement(By.tagName("body")).getText());
    }
}
