package org.wildfly.swarm.ejb;

import org.junit.Test;
import org.wildfly.swarm.container.Container;

/**
 * @author Bob McWhirter
 */
public class EJBInVmTest {

    @Test
    public void testSimple() throws Exception {
        Container container = new Container();
        container.fraction( new EJBFraction() );
        container.start().stop();
    }
}
