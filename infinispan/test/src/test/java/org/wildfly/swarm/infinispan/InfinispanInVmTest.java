package org.wildfly.swarm.infinispan;

import org.junit.Test;
import org.wildfly.swarm.container.Container;

/**
 * @author Lance Ball
 */
public class InfinispanInVmTest {

    @Test
    public void testSimple() throws Exception {
        Container container = new Container();
        container.fraction( InfinispanFraction.createDefaultFraction() );
        container.start().stop();
    }
}
