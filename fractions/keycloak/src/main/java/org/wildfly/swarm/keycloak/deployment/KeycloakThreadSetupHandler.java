package org.wildfly.swarm.keycloak.deployment;

import io.undertow.server.HttpServerExchange;
import io.undertow.servlet.api.ThreadSetupHandler;
import org.keycloak.KeycloakSecurityContext;
import org.keycloak.adapters.undertow.OIDCUndertowHttpFacade;

/**
 * @author Bob McWhirter
 */
class KeycloakThreadSetupHandler implements ThreadSetupHandler {
    @Override
    public <T, C> Action<T, C> create(final Action<T, C> action) {

        return new Action<T, C>() {

            @Override
            public T call(HttpServerExchange exchange, C context) throws Exception {
                if (exchange == null) {
                    return null;
                }
                KeycloakSecurityContext c = exchange.getAttachment(OIDCUndertowHttpFacade.KEYCLOAK_SECURITY_CONTEXT_KEY);
                KeycloakSecurityContextAssociation.associate(c);
                try {
                    return action.call(exchange, context);
                } finally {
                    KeycloakSecurityContextAssociation.disassociate();
                }
            }
        };
    }

}
