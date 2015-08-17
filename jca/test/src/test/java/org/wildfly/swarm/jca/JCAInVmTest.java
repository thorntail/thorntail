package org.wildfly.swarm.jca;

import org.junit.Ignore;
import org.junit.Test;
import org.wildfly.swarm.container.Container;
import org.wildfly.swarm.naming.NamingFraction;

/**
 * @author Bob McWhirter
 */
public class JCAInVmTest {

    @Test
    public void testSimple() throws Exception {
        Container container = new Container();
        container.fraction( new JCAFraction() );
        container.start().stop();
    }
}
