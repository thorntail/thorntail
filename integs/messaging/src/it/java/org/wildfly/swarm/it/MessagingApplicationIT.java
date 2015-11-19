package org.wildfly.swarm.it;

import java.util.List;

import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author Bob McWhirter
 */
@RunWith(Arquillian.class)
public class MessagingApplicationIT extends AbstractIntegrationTest {

    @Drone
    WebDriver browser;

    @Test
    public void testIt() throws Exception {
        browser.navigate().to("http://localhost:8080/");
        assertThat(browser.getPageSource()).contains("Howdy!");
        Log stdout = getLog("target/stdout.log");
        assertThatLog( stdout ).hasLineContaining( "Hello!" );
        //assertThat( lines.stream().filter( e->e.contains( "Hello!" ) ).count() ).isGreaterThan( 0 );
    }
}
