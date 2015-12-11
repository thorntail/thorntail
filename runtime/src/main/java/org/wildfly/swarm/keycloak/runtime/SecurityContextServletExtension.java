/**
 * Copyright 2015 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wildfly.swarm.keycloak.runtime;

import javax.servlet.ServletContext;

import io.undertow.server.HandlerWrapper;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.servlet.ServletExtension;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.ThreadSetupAction;
import org.keycloak.KeycloakSecurityContext;
import org.keycloak.adapters.undertow.OIDCServletUndertowHttpFacade;
import org.keycloak.adapters.undertow.OIDCUndertowHttpFacade;

/**
 * @author Bob McWhirter
 */
public class SecurityContextServletExtension implements ServletExtension {
    @Override
    public void handleDeployment(DeploymentInfo info, ServletContext context) {
        System.err.println( "HANDLE DEPLOYMENT FOR SECURITY CONTEXT" );
        info.addThreadSetupAction(new ThreadSetupAction() {
            @Override
            public Handle setup(HttpServerExchange exchange) {
                if (exchange == null) {
                    return null;
                }
                KeycloakSecurityContext c = exchange.getAttachment(OIDCUndertowHttpFacade.KEYCLOAK_SECURITY_CONTEXT_KEY);
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
                        KeycloakSecurityContext c = exchange.getAttachment(OIDCUndertowHttpFacade.KEYCLOAK_SECURITY_CONTEXT_KEY);
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
