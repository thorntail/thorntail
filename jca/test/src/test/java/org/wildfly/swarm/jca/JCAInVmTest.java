package org.wildfly.swarm.jca;

import org.junit.Test;
import org.wildfly.swarm.container.Container;

/**
 * @author Bob McWhirter
 */
public class JCAInVmTest {

    @Test
    public void testSimple() throws Exception {
        Container container = new Container();
        container.fraction( JCAFraction.createDefaultFraction() );
        container.start().stop();
    }
}
