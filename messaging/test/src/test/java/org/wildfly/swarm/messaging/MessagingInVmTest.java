package org.wildfly.swarm.messaging;

import org.junit.Test;
import org.wildfly.swarm.container.Container;

/**
 * @author Bob McWhirter
 */
public class MessagingInVmTest {

    @Test
    public void testSimple() throws Exception {
        Container container = new Container();
        container.fraction( new MessagingFraction() );
        container.start().stop();
    }
}
