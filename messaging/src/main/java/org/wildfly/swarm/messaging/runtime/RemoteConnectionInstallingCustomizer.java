package org.wildfly.swarm.messaging.runtime;

import java.util.Optional;

import javax.enterprise.inject.Any;
import javax.inject.Inject;

import org.wildfly.swarm.messaging.MessagingFraction;
import org.wildfly.swarm.messaging.MessagingProperties;
import org.wildfly.swarm.spi.api.Customizer;
import org.wildfly.swarm.spi.runtime.annotations.ConfigurationValue;
import org.wildfly.swarm.spi.runtime.annotations.Pre;

/** Installs a remote connection based upon properties or YAML configuration.
 *
 * @see MessagingProperties#REMOTE_MQ_NAME
 * @see MessagingProperties#REMOTE_HOST
 * @see MessagingProperties#REMOTE_PORT
 * @see MessagingProperties#REMOTE_JNDI_NAME
 *
 * @author Bob McWhirter
 */
@Pre
public class RemoteConnectionInstallingCustomizer implements Customizer {

    @Inject
    @Any
    MessagingFraction fraction;

    @Inject
    @ConfigurationValue(MessagingProperties.REMOTE_MQ_NAME)
    Optional<Boolean> remote = Optional.empty();

    @Inject
    @ConfigurationValue(MessagingProperties.REMOTE_MQ_NAME)
    Optional<String> remoteMqName = Optional.empty();

    @Inject
    @ConfigurationValue(MessagingProperties.REMOTE_JNDI_NAME)
    Optional<String> jndiName = Optional.empty();

    @Inject
    @ConfigurationValue(MessagingProperties.REMOTE_HOST)
    Optional<String> remoteHost = Optional.empty();

    @Inject
    @ConfigurationValue(MessagingProperties.REMOTE_PORT)
    Optional<String> remotePort = Optional.empty();

    @Override
    public void customize() {
        if ( this.remote.isPresent() || this.remoteMqName.isPresent() || this.jndiName.isPresent()|| this.remoteHost.isPresent()|| this.remotePort.isPresent() ) {

            fraction.defaultServer( (server)->{
                String mqName = this.remoteMqName.orElse( MessagingProperties.DEFAULT_REMOTE_MQ_NAME );

                server.remoteConnection( mqName, (config)->{
                    this.jndiName.ifPresent(config::jndiName);
                    this.remoteHost.ifPresent(config::host);
                    this.remotePort.ifPresent(config::port);
                });
            });
        }

    }
}
