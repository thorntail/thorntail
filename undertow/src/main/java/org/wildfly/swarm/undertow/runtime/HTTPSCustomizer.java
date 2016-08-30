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

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.wildfly.swarm.config.ManagementCoreService;
import org.wildfly.swarm.config.undertow.Server;
import org.wildfly.swarm.internal.SwarmMessages;
import org.wildfly.swarm.spi.api.Customizer;
import org.wildfly.swarm.spi.runtime.annotations.Pre;
import org.wildfly.swarm.undertow.UndertowFraction;
import org.wildfly.swarm.undertow.descriptors.CertInfo;

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

    @Inject
    private CertInfo certInfo;

    public void customize() {
        if (!this.managementCoreService.isUnsatisfied()) {
            UndertowFraction fraction = undertowInstance.get();
            if (certInfo.isValid()) {
                ManagementCoreService management = this.managementCoreService.get();
                if (management == null) {
                    throw SwarmMessages.MESSAGES.httpsRequiresManagementFraction();
                }

                for (Server server : fraction.subresources().servers()) {
                    if (server.subresources().httpsListeners().isEmpty()) {
                        server.httpsListener("default-https", (listener) -> {
                            listener.securityRealm("SSLRealm")
                                    .socketBinding("https");
                        });
                    }
                }

                management.securityRealm("SSLRealm", (realm) -> {
                    realm.sslServerIdentity((identity) -> {
                        identity.keystorePath(certInfo.keystorePath())
                                .keystoreRelativeTo(certInfo.keystoreRelativeTo())
                                .keystorePassword(certInfo.keystorePassword())
                                .keyPassword(certInfo.keyPassword())
                                .alias(certInfo.keystoreAlias())
                                .generateSelfSignedCertificateHost(certInfo.generateSelfSignedCertificateHost());
                    });
                });
            }
        }
    }
}
