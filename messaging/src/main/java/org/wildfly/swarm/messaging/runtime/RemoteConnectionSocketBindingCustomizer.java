package org.wildfly.swarm.messaging.runtime;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.wildfly.swarm.config.messaging.activemq.Server;
import org.wildfly.swarm.messaging.EnhancedServer;
import org.wildfly.swarm.messaging.MessagingFraction;
import org.wildfly.swarm.messaging.MessagingProperties;
import org.wildfly.swarm.spi.api.Customizer;
import org.wildfly.swarm.spi.api.OutboundSocketBinding;
import org.wildfly.swarm.spi.api.SocketBindingGroup;
import org.wildfly.swarm.spi.runtime.annotations.ConfigurationValue;
import org.wildfly.swarm.spi.runtime.annotations.Post;
import org.wildfly.swarm.spi.runtime.annotations.Pre;

/** Creates an outbound-socket binding for each RemoteConnection.
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

    @Inject
    @ConfigurationValue(MessagingProperties.REMOTE_MQ_NAME)
    String mqName;

    @Inject
    @ConfigurationValue(MessagingProperties.REMOTE_HOST)
    String remoteHost;

    @Inject
    @ConfigurationValue(MessagingProperties.REMOTE_PORT)
    String remotePort;

    @Override
    public void customize() {
        List<Server> servers = fraction.subresources().servers();

        String mqName = this.mqName;
        if (mqName == null) {
            mqName = MessagingProperties.DEFAULT_REMOTE_MQ_NAME;
        }

        String finalMqName = mqName;

        servers.stream()
                .filter(e -> e instanceof EnhancedServer)
                .forEach(server -> {
                    ((EnhancedServer) server).remoteConnections()
                            .forEach(connection -> {
                                OutboundSocketBinding binding = new OutboundSocketBinding(connection.name());
                                if (connection.name().equals(finalMqName)) {
                                    String host = this.remoteHost;
                                    if (host == null) {
                                        host = connection.host();
                                    }

                                    String port = this.remotePort;
                                    if (port == null) {
                                        port = connection.port();
                                    }

                                    binding.remoteHost(host)
                                            .remotePort(port);
                                } else {
                                    binding.remoteHost(connection.host())
                                            .remotePort(connection.port());
                                }

                                group.outboundSocketBinding(binding);
                            });
                });

    }
}
