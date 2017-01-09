package org.wildfly.swarm.servers.keycloak;

import org.wildfly.swarm.Swarm;

/**
 * @author Ken Finnigan
 */
public class KeycloakServer {

    protected KeycloakServer() {
    }

    public static void main(String... args) throws Exception {
        (new Swarm()).start();
    }
}
