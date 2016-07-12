package org.wildfly.swarm.servers.keycloak;

import org.wildfly.swarm.container.Container;

/**
 * @author Ken Finnigan
 */
public class KeycloakServer {
    public static void main(String... args) throws Exception {
        (new Container()).start(true);
    }
}
