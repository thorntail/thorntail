package org.wildfly.swarm.jpa;

import org.junit.Test;
import org.wildfly.swarm.container.Container;

/**
 * @author Bob McWhirter
 */
public class JPAInVmTest {

    @Test
    public void testSimple() throws Exception {
        Container container = new Container();
        container.fraction( JPAFraction.createDefaultFraction() );
        container.start().stop();
    }
}
