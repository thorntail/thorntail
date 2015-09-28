package org.wildfly.swarm.ejb.remote;

import org.junit.Test;
import org.wildfly.swarm.container.Container;

/**
 * @author Ken Finnigan
 */
public class EJBRemoteInVmTest {

    @Test
    public void testSimple() throws Exception {
        Container container = new Container();
        container.fraction( new EJBRemoteFraction() );
        container.start().stop();
    }
}
