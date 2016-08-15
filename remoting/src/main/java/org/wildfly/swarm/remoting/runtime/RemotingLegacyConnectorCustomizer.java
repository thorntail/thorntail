package org.wildfly.swarm.remoting.runtime;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.jboss.logging.Logger;
import org.wildfly.swarm.container.runtime.config.DefaultSocketBindingGroupProducer;
import org.wildfly.swarm.remoting.RemotingFraction;
import org.wildfly.swarm.remoting.RemotingProperties;
import org.wildfly.swarm.spi.api.Customizer;
import org.wildfly.swarm.spi.api.SocketBinding;
import org.wildfly.swarm.spi.api.SocketBindingGroup;
import org.wildfly.swarm.spi.runtime.annotations.ConfigurationValue;
import org.wildfly.swarm.spi.runtime.annotations.Post;

/** Configures the legacy remoting socketing binding and connector if required.
 *
 * <p>In the event {@link RemotingFraction#requireLegacyConnector(boolean)}</p> has been
 * set to <code>true</code> or if configuration property <code>swarm.remoting.port</code>
 * is set to any value, this customizer will install a socket-binding named
 * <code>legacy-remoting</code> for port <code>4777</code> or whatever value
 * configuration property <code>swarm.remoting.port</code> is set to.</p>
 *
 * @author Bob McWhirter
 */
@Post
@Singleton
public class RemotingLegacyConnectorCustomizer implements Customizer {

    private static Logger LOG = Logger.getLogger("org.wildfly.swarm.remoting");

    /** Default legacy remoting port. */
    public static final int DEFAULT_LEGACY_REMOTING_PORT = 4777;

    @Inject
    private RemotingFraction remoting;

    @Inject
    @ConfigurationValue(RemotingProperties.REMOTING_PORT)
    private Integer port;

    @Inject
    @Named(DefaultSocketBindingGroupProducer.STANDARD_SOCKETS)
    private SocketBindingGroup group;

    @Override
    public void customize() {
        if (this.remoting.isRequireLegacyConnector() || this.port != null ) {
            LOG.info("Remoting installed but Undertnow not available. Enabled legacy connector on port 4777.");
            this.remoting.connector("legacy", (connector) -> {
                connector.socketBinding("legacy-remoting");
            });
            if ( this.port == null ) {
                this.port = DEFAULT_LEGACY_REMOTING_PORT;

            }
            group.socketBinding(new SocketBinding("legacy-remoting").port(this.port));
        }
    }
}
