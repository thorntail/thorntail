package org.wildfly.swarm.undertow;

import org.junit.Test;
import org.wildfly.swarm.container.Container;

/**
 * @author Bob McWhirter
 */
public class UndertowInVmTest {

    @Test
    public void testSimple() throws Exception {
        Container container = new Container();
        container.fraction( new UndertowFraction() );
        container.start().stop();
    }
}
