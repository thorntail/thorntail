package org.wildfly.swarm.undertow;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.wildfly.swarm.config.ManagementCoreService;
import org.wildfly.swarm.config.undertow.Server;

/**
 * @author Bob McWhirter
 */
@ApplicationScoped
public class HTTPSCustomizer {

    @Inject
    @Any
    private Instance<UndertowFraction> undertowInstance;

    @Inject
    @Any
    private Instance<ManagementCoreService> managementCoreService;

    public HTTPSCustomizer() {
        System.err.println("construct HTTPSCustomizer");
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
