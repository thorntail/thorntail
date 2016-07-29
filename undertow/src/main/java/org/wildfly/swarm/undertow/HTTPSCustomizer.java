package org.wildfly.swarm.undertow;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.wildfly.swarm.config.ManagementCoreService;
import org.wildfly.swarm.config.undertow.Server;
import org.wildfly.swarm.spi.api.Customizer;
import org.wildfly.swarm.undertow.UndertowFraction;

/**
 * @author Bob McWhirter
 */
@ApplicationScoped
public class HTTPSCustomizer {

    @Inject
    private UndertowFraction fraction;

    @Inject @Any
    private Instance<ManagementCoreService> managementCoreService;

    public HTTPSCustomizer() {
        System.err.println( "construct HTTPSCustomizer" );
    }

    public void customize() {
        /*
        if ( ! this.managementCoreService.isUnsatisfied() ) {
            if (this.fraction.keystorePassword() != null & this.fraction.keystorePassword() != null && this.fraction.alias() != null) {
                ManagementCoreService management = this.managementCoreService.get();
                if (management == null) {
                    throw new RuntimeException("HTTPS configured but org.wildfly.swarm:management not available");
                }

                List<Server> servers = this.fraction.subresources().servers();

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
                        identity.keystorePath(this.fraction.keystorePath());
                        identity.keystorePassword(this.fraction.keystorePassword());
                        identity.alias(this.fraction.alias());
                    });
                });
            }
        }
        */
    }

}
