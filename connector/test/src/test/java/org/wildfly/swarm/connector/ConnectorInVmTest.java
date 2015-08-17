package org.wildfly.swarm.connector;

import org.junit.Test;
import org.wildfly.swarm.container.Container;

/**
 * @author Bob McWhirter
 */
public class ConnectorInVmTest {

    @Test
    public void testSimple() throws Exception {
        Container container = new Container();
        container.fraction( new ConnectorFraction() );
        container.start().stop();
    }
}
