package org.wildfly.swarm.logging;

import org.junit.Test;
import org.wildfly.swarm.container.Container;

/**
 * @author Bob McWhirter
 */
public class LoggingInVmTest {

    @Test
    public void testSimple() throws Exception {
        Container container = new Container();
        container.fraction( new LoggingFraction() );
        container.start().stop();
    }
}
