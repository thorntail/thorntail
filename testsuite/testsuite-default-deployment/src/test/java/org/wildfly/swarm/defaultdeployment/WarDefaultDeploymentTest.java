package org.wildfly.swarm.defaultdeployment;

import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.swarm.arquillian.DefaultDeployment;

/**
 * @author Bob McWhirter
 */
@RunWith(Arquillian.class)
@DefaultDeployment(main = Main.class)
public class WarDefaultDeploymentTest {

    @Test
    @RunAsClient
    public void testNothing() {
        // because the test is in the main();
    }
}
