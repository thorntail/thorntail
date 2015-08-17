package org.wildfly.swarm.naming;

import org.junit.Test;
import org.wildfly.swarm.container.Container;
import org.wildfly.swarm.jsf.JSFFraction;

/**
 * @author Bob McWhirter
 */
public class JSFInVmTest {

    @Test
    public void testSimple() throws Exception {
        Container container = new Container();
        container.fraction( new JSFFraction() );
        container.start().stop();
    }
}
