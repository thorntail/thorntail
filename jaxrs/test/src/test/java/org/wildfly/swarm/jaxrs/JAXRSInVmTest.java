package org.wildfly.swarm.jaxrs;

import org.junit.Test;
import org.wildfly.swarm.container.Container;

/**
 * @author Bob McWhirter
 */
public class JAXRSInVmTest {

    @Test
    public void testSimple() throws Exception {
        Container container = new Container();
        container.fraction( new JAXRSFraction() );
        container.start().stop();
    }
}
