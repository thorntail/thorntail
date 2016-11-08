package org.wildfly.swarm.messaging.runtime;

import java.util.List;

import javax.enterprise.inject.Any;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.wildfly.swarm.config.messaging.activemq.Server;
import org.wildfly.swarm.messaging.EnhancedServer;
import org.wildfly.swarm.messaging.MessagingFraction;
import org.wildfly.swarm.spi.api.Customizer;
import org.wildfly.swarm.spi.runtime.annotations.Post;

/** Performs re-configuration of the MessagingFraction for available RemoteConnections.
 *
 * @author Bob McWhirter
 */
@Post
public class RemoteConnectionCustomizer implements Customizer {

    @Inject
    MessagingFraction fraction;

    @Override
    public void customize() {
        List<Server> servers = fraction.subresources().servers();

        servers.stream()
                .filter(e -> e instanceof EnhancedServer)
                .forEach(server -> {
                    ((EnhancedServer) server).remoteConnections()
                            .forEach(connection -> {
                                server.remoteConnector(connection.name(), connector -> {
                                    connector.socketBinding(connection.name());
                                });

                                server.pooledConnectionFactory(connection.name(), factory -> {
                                    factory.connectors(connection.name());
                                    factory.entry(connection.jndiName());
                                });
                            });
                });

    }
}
