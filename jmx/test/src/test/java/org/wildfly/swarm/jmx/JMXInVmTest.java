package org.wildfly.swarm.jmx;

import org.junit.Ignore;
import org.junit.Test;
import org.wildfly.swarm.container.Container;

/**
 * @author Bob McWhirter
 */
public class JMXInVmTest {

    @Test
    public void testSimple() throws Exception {
        Container container = new Container();
        container.fraction( new JMXFraction() );
        container.start().stop();
    }
}
