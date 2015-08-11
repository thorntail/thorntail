package org.wildfly.swarm.keycloak.runtime;

import javax.servlet.ServletContext;

import io.undertow.server.HandlerWrapper;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.servlet.ServletExtension;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.ThreadSetupAction;
import org.jboss.security.SecurityContextAssociation;
import org.keycloak.KeycloakSecurityContext;
import org.keycloak.adapters.undertow.UndertowHttpFacade;

/**
 * @author Bob McWhirter
 */
public class SecurityContextServletExtension implements ServletExtension {
    @Override
    public void handleDeployment(DeploymentInfo info, ServletContext context) {
        info.addThreadSetupAction(new ThreadSetupAction() {
            @Override
            public Handle setup(HttpServerExchange exchange) {
                if (exchange == null) {
                    return null;
                }
                KeycloakSecurityContext c = exchange.getAttachment(UndertowHttpFacade.KEYCLOAK_SECURITY_CONTEXT_KEY);
                KeycloakSecurityContextAssociation.associate(c);
                return new Handle() {
                    @Override
                    public void tearDown() {
                        KeycloakSecurityContextAssociation.disassociate();
                    }
                };
            }
        });

        info.addInnerHandlerChainWrapper(new HandlerWrapper() {
            @Override
            public HttpHandler wrap(HttpHandler next) {
                return new HttpHandler() {
                    @Override
                    public void handleRequest(HttpServerExchange exchange) throws Exception {
                        KeycloakSecurityContext c = exchange.getAttachment(UndertowHttpFacade.KEYCLOAK_SECURITY_CONTEXT_KEY);
                        if ( c != null ) {
                            KeycloakSecurityContextAssociation.associate(c);
                        }

                        try {
                            next.handleRequest(exchange);
                        } finally {
                            KeycloakSecurityContextAssociation.disassociate();
                        }
                    }
                };
            }
        });
    }
}
