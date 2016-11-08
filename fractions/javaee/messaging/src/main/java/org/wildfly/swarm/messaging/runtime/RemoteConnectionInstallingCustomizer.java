package org.wildfly.swarm.messaging.runtime;

import javax.enterprise.inject.Any;
import javax.inject.Inject;

import org.wildfly.swarm.messaging.MessagingFraction;
import org.wildfly.swarm.spi.api.Configurable;
import org.wildfly.swarm.spi.api.Customizer;
import org.wildfly.swarm.spi.runtime.annotations.Pre;

import static org.wildfly.swarm.messaging.MessagingProperties.DEFAULT_REMOTE_HOST;
import static org.wildfly.swarm.messaging.MessagingProperties.DEFAULT_REMOTE_MQ_NAME;
import static org.wildfly.swarm.messaging.MessagingProperties.DEFAULT_REMOTE_PORT;
import static org.wildfly.swarm.spi.api.Configurable.ifAnyExplicitlySet;
import static org.wildfly.swarm.spi.api.Configurable.integer;
import static org.wildfly.swarm.spi.api.Configurable.string;

/** Installs a remote connection based upon properties or YAML configuration.
 *
 * @author Bob McWhirter
 */
@Pre
public class RemoteConnectionInstallingCustomizer implements Customizer {

    final Configurable<String> name = string("swarm.messaging.remote.name", DEFAULT_REMOTE_MQ_NAME );
    final Configurable<String> host = string("swarm.messaging.remote.host", DEFAULT_REMOTE_HOST);
    final Configurable<Integer> port = integer("swarm.messaging.remote.port", DEFAULT_REMOTE_PORT);
    final Configurable<String> jndiName = string("swarm.messaging.remote.jndi-name", ()->"java:/jms/" + name.get() );

    final Configurable<Boolean> enabled = ifAnyExplicitlySet( "swarm.messaging.remote", name, host, port, jndiName );

    @Inject
    @Any
    MessagingFraction fraction;

    @Override
    public void customize() {
        if ( this.enabled.get() ) {
            fraction.defaultServer( (server)->{
                server.remoteConnection( name.get(), (connection)->{
                    connection.jndiName( this.jndiName.get() );
                    connection.host( this.host.get() );
                    connection.port( this.port.get() );
                });
            });
        }

    }
}
