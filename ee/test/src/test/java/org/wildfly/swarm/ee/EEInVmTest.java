package org.wildfly.swarm.ee;

import org.junit.Test;
import org.wildfly.swarm.container.Container;

/**
 * @author Bob McWhirter
 */
public class EEInVmTest {

    @Test
    public void testSimple() throws Exception {
        Container container = new Container();
        container.fraction( new EEFraction() );
        container.start().stop();
    }
}
