/**
 * Copyright 2015-2017 Red Hat, Inc, and individual contributors.
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
package org.wildfly.swarm.keycloak.deployment;

import io.undertow.servlet.api.ThreadSetupHandler;
import org.keycloak.KeycloakSecurityContext;
import org.keycloak.adapters.undertow.OIDCUndertowHttpFacade;

/**
 * @author Bob McWhirter
 */
class KeycloakThreadSetupHandler implements ThreadSetupHandler {
    @Override
    public <T, C> Action<T, C> create(final Action<T, C> action) {
        return (exchange, context) -> {
            if (exchange == null) {
                return action.call(exchange, context);
            }
            KeycloakSecurityContext c = exchange.getAttachment(OIDCUndertowHttpFacade.KEYCLOAK_SECURITY_CONTEXT_KEY);
            KeycloakSecurityContextAssociation.associate(c);
            try {
                return action.call(exchange, context);
            } finally {
                KeycloakSecurityContextAssociation.disassociate();
            }
        };
    }
}
