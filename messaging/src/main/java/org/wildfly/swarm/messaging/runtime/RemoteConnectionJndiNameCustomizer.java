package org.wildfly.swarm.messaging.runtime;

import java.util.List;

import javax.enterprise.inject.Any;
import javax.inject.Inject;

import org.wildfly.swarm.config.messaging.activemq.Server;
import org.wildfly.swarm.messaging.EnhancedServer;
import org.wildfly.swarm.messaging.MessagingFraction;
import org.wildfly.swarm.messaging.MessagingProperties;
import org.wildfly.swarm.spi.api.Customizer;
import org.wildfly.swarm.spi.runtime.annotations.ConfigurationValue;
import org.wildfly.swarm.spi.runtime.annotations.Post;

/** Overrides JNDI name of the connection factory from a property/YAML configuration value.
 *
 * @see MessagingProperties#REMOTE_JNDI_NAME
 *
 * @author Bob McWhirter
 */
@Post
public class RemoteConnectionJndiNameCustomizer implements Customizer {

    @Inject
    @Any
    MessagingFraction fraction;

    @Inject
    @ConfigurationValue(MessagingProperties.REMOTE_MQ_NAME)
    String mqName;

    @Inject
    @ConfigurationValue(MessagingProperties.REMOTE_JNDI_NAME)
    String jndiName;

    @Override
    public void customize() {
        List<Server> servers = fraction.subresources().servers();

        String mqName = this.mqName;
        if ( mqName == null ) {
            mqName = MessagingProperties.DEFAULT_REMOTE_MQ_NAME;
        }

        String finalMqName = mqName;
        servers.stream()
                .filter(e -> e instanceof EnhancedServer)
                .forEach( server->{
                    ((EnhancedServer) server).remoteConnections()
                            .stream()
                            .filter( e->e.name().equals( finalMqName ) )
                            .findFirst()
                            .ifPresent(connection -> {
                                String jndiName = this.jndiName;
                                if ( jndiName != null ) {
                                    connection.jndiName( jndiName );
                                }
                            });
                });

    }
}
