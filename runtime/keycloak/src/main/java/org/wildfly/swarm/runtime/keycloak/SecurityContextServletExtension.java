package org.wildfly.swarm.runtime.keycloak;

import io.undertow.server.HandlerWrapper;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.servlet.ServletExtension;
import io.undertow.servlet.api.DeploymentInfo;
import org.keycloak.KeycloakSecurityContext;
import org.keycloak.adapters.KeycloakAccount;
import org.keycloak.adapters.undertow.UndertowHttpFacade;

import javax.servlet.ServletContext;

/**
 * @author Bob McWhirter
 */
public class SecurityContextServletExtension implements ServletExtension {
    @Override
    public void handleDeployment(DeploymentInfo info, ServletContext context) {
        info.addInnerHandlerChainWrapper(new HandlerWrapper() {
            @Override
            public HttpHandler wrap(HttpHandler next) {
                return new HttpHandler() {
                    @Override
                    public void handleRequest(HttpServerExchange exchange) throws Exception {
                        KeycloakSecurityContext c = exchange.getAttachment(UndertowHttpFacade.KEYCLOAK_SECURITY_CONTEXT_KEY);
                        System.err.println( " -----> " + c.getTokenString() );
                        next.handleRequest(exchange);
                    }
                };
            }
        });
    }
}
