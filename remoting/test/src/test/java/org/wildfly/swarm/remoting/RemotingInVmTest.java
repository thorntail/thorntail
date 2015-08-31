package org.wildfly.swarm.remoting;

import org.junit.Test;
import org.wildfly.swarm.container.Container;

/**
 * @author Ken Finnigan
 */
public class RemotingInVmTest {

    @Test
    public void testSimple() throws Exception {
        Container container = new Container();
        container.fraction(new RemotingFraction());
        container.start().stop();
    }
}
