package org.wildfly.swarm.messaging.runtime;

import javax.enterprise.inject.Any;
import javax.inject.Inject;
import javax.inject.Singleton;

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
    Boolean remote;

    @Inject
    @ConfigurationValue(MessagingProperties.REMOTE_MQ_NAME)
    String remoteMqName;

    @Inject
    @ConfigurationValue(MessagingProperties.REMOTE_JNDI_NAME)
    String jndiName;

    @Inject
    @ConfigurationValue(MessagingProperties.REMOTE_HOST)
    String remoteHost;

    @Inject
    @ConfigurationValue(MessagingProperties.REMOTE_PORT)
    String remotePort;

    @Override
    public void customize() {
        if ( ( this.remote != null && this.remote ) || this.remoteMqName != null || this.jndiName != null || this.remoteHost != null || this.remotePort != null ) {

            fraction.defaultServer( (server)->{
                String mqName = this.remoteMqName;

                if ( mqName == null ) {
                    mqName = MessagingProperties.DEFAULT_REMOTE_MQ_NAME;
                }

                server.remoteConnection( mqName, (config)->{
                    if ( this.jndiName != null ) {
                        config.jndiName( this.jndiName );
                    }

                    if ( this.remoteHost != null ) {
                        config.host( this.remoteHost );
                    }

                    if ( this.remotePort != null ) {
                        config.port( this.remotePort );
                    }
                });
            });
        }

    }
}
