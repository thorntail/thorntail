package org.wildfly.swarm.messaging.runtime;

import java.util.List;

import javax.enterprise.inject.Any;
import javax.inject.Inject;
import javax.inject.Named;

import org.wildfly.swarm.config.messaging.activemq.Server;
import org.wildfly.swarm.messaging.EnhancedServer;
import org.wildfly.swarm.messaging.MessagingFraction;
import org.wildfly.swarm.spi.api.Customizer;
import org.wildfly.swarm.spi.api.OutboundSocketBinding;
import org.wildfly.swarm.spi.api.SocketBindingGroup;
import org.wildfly.swarm.spi.runtime.annotations.Post;

/**
 * Creates an outbound-socket binding for each RemoteConnection.
 *
 * @author Bob McWhirter
 */
@Post
public class RemoteConnectionSocketBindingCustomizer implements Customizer {

    @Inject
    @Named("standard-sockets")
    SocketBindingGroup group;

    @Inject
    @Any
    MessagingFraction fraction;

    @Override
    public void customize() {
        List<Server> servers = fraction.subresources().servers();

        servers.stream()
                .filter(e -> e instanceof EnhancedServer)
                .forEach(server -> {
                    ((EnhancedServer) server).remoteConnections()
                            .forEach(connection -> {
                                OutboundSocketBinding binding = new OutboundSocketBinding(connection.name());
                                binding.remoteHost(connection.host())
                                        .remotePort(connection.port());

                                group.outboundSocketBinding(binding);
                            });
                });

    }
}
