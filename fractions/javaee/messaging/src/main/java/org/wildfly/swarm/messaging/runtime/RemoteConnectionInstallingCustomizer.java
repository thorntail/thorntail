package org.wildfly.swarm.messaging.runtime;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.inject.Inject;

import org.wildfly.swarm.config.runtime.AttributeDocumentation;
import org.wildfly.swarm.messaging.MessagingFraction;
import org.wildfly.swarm.spi.api.Customizer;
import org.wildfly.swarm.spi.api.Defaultable;
import org.wildfly.swarm.spi.api.annotations.Configurable;
import org.wildfly.swarm.spi.runtime.annotations.Pre;

import static org.wildfly.swarm.messaging.MessagingProperties.DEFAULT_REMOTE_HOST;
import static org.wildfly.swarm.messaging.MessagingProperties.DEFAULT_REMOTE_MQ_NAME;
import static org.wildfly.swarm.messaging.MessagingProperties.DEFAULT_REMOTE_PORT;
import static org.wildfly.swarm.spi.api.Defaultable.ifAnyExplicitlySet;
import static org.wildfly.swarm.spi.api.Defaultable.integer;
import static org.wildfly.swarm.spi.api.Defaultable.string;

/**
 * Installs a remote connection based upon properties or YAML configuration.
 *
 * @author Bob McWhirter
 */
@Pre
@ApplicationScoped
public class RemoteConnectionInstallingCustomizer implements Customizer {

    @AttributeDocumentation("Name of the remote connection")
    @Configurable("thorntail.messaging.remote.name")
    final Defaultable<String> name = string(DEFAULT_REMOTE_MQ_NAME);

    @AttributeDocumentation("Host of the remote connection")
    @Configurable("thorntail.messaging.remote.host")
    final Defaultable<String> host = string(DEFAULT_REMOTE_HOST);

    @AttributeDocumentation("Port of the remote connection")
    @Configurable("thorntail.messaging.remote.port")
    final Defaultable<Integer> port = integer(DEFAULT_REMOTE_PORT);

    @AttributeDocumentation("JNDI name of the remote connection")
    @Configurable("thorntail.messaging.remote.jndi-name")
    final Defaultable<String> jndiName = string(() -> "java:/jms/" + name.get());

    @AttributeDocumentation("Flag to enable the remote connection")
    @Configurable("thorntail.messaging.remote")
    final Defaultable<Boolean> enabled = ifAnyExplicitlySet(name, host, port, jndiName);

    @Inject
    @Any
    MessagingFraction fraction;

    @Override
    public void customize() {
        if (this.enabled.get()) {
            fraction.defaultServer((server) -> {
                server.remoteConnection(name.get(), (connection) -> {
                    connection.jndiName(this.jndiName.get());
                    connection.host(this.host.get());
                    connection.port(this.port.get());
                });
            });
        }

    }
}
