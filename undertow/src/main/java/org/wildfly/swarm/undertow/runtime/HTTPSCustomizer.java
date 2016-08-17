/**
 * Copyright 2015-2016 Red Hat, Inc, and individual contributors.
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
package org.wildfly.swarm.undertow.runtime;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.wildfly.swarm.config.ManagementCoreService;
import org.wildfly.swarm.config.undertow.Server;
import org.wildfly.swarm.spi.api.Customizer;
import org.wildfly.swarm.spi.runtime.annotations.Pre;
import org.wildfly.swarm.undertow.UndertowFraction;

/**
 * @author Bob McWhirter
 */
@Pre
@Singleton
public class HTTPSCustomizer implements Customizer {

    @Inject
    @Any
    private Instance<UndertowFraction> undertowInstance;

    @Inject
    @Any
    private Instance<ManagementCoreService> managementCoreService;

    public HTTPSCustomizer() {
    }

    public void customize() {
        if (!this.managementCoreService.isUnsatisfied()) {
            UndertowFraction fraction = undertowInstance.get();
            if (fraction.keystorePassword() != null & fraction.keystorePassword() != null && fraction.alias() != null) {
                ManagementCoreService management = this.managementCoreService.get();
                if (management == null) {
                    throw new RuntimeException("HTTPS configured but org.wildfly.swarm:management not available");
                }

                List<Server> servers = fraction.subresources().servers();

                for (Server server : servers) {
                    if (server.subresources().httpsListeners().isEmpty()) {
                        server.httpsListener("default-https", (listener) -> {
                            listener.securityRealm("SSLRealm");
                            listener.socketBinding("https");
                        });
                    }
                }

                management.securityRealm("SSLRealm", (realm) -> {
                    realm.sslServerIdentity((identity) -> {
                        identity.keystorePath(fraction.keystorePath());
                        identity.keystorePassword(fraction.keystorePassword());
                        identity.alias(fraction.alias());
                    });
                });
            }
        }
    }

}
