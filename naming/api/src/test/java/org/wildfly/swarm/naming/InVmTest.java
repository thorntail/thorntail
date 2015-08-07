package org.wildfly.swarm.naming;

import org.junit.Ignore;
import org.junit.Test;
import org.wildfly.swarm.container.Container;

/**
 * @author Bob McWhirter
 */
public class InVmTest {

    @Ignore
    @Test
    public void testContainer() throws Exception {
        Container container = new Container();
        container.fraction(new NamingFraction());
        container.start();
        container.stop();
    }
}
