package org.wildfly.swarm.naming;

import org.junit.Ignore;
import org.junit.Test;
import org.wildfly.swarm.container.Container;

/**
 * @author Bob McWhirter
 */
public class NamingInVmTest {

    @Test
    public void testSimple() throws Exception {
        Container container = new Container();
        container.fraction( new NamingFraction() );
        container.start().stop();
    }
}
