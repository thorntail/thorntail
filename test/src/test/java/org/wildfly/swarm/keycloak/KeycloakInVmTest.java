package org.wildfly.swarm.keycloak;

import org.junit.Test;
import org.wildfly.swarm.container.Container;

/**
 * @author Bob McWhirter
 */
public class KeycloakInVmTest {

    @Test
    public void testSimple() throws Exception {
        Container container = new Container();
        container.fraction( new KeycloakFraction() );
        container.start().stop();
    }
}
