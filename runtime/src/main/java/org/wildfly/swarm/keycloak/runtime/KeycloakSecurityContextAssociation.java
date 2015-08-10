package org.wildfly.swarm.keycloak.runtime;

import org.keycloak.KeycloakSecurityContext;

/**
 * @author Bob McWhirter
 */
public class KeycloakSecurityContextAssociation {

    private static ThreadLocal<KeycloakSecurityContext> SECURITY_CONTEXT = new ThreadLocal<>();

    public static KeycloakSecurityContext get() {
        KeycloakSecurityContext context = SECURITY_CONTEXT.get();
        return context;
    }

    public static void associate(KeycloakSecurityContext context) {
        SECURITY_CONTEXT.set(context);
    }

    public static void disassociate() {
        SECURITY_CONTEXT.remove();
    }
}
