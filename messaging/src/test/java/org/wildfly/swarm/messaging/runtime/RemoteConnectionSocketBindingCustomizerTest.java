package org.wildfly.swarm.messaging.runtime;

import org.junit.Before;
import org.junit.Test;
import org.wildfly.swarm.messaging.MessagingFraction;
import org.wildfly.swarm.messaging.MessagingProperties;
import org.wildfly.swarm.spi.api.OutboundSocketBinding;
import org.wildfly.swarm.spi.api.SocketBindingGroup;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author Bob McWhirter
 */
public class RemoteConnectionSocketBindingCustomizerTest {

    private SocketBindingGroup group;

    private MessagingFraction fraction;

    private RemoteConnectionSocketBindingCustomizer customizer;

    @Before
    public void setUp() {
        this.group = new SocketBindingGroup("standard-sockets", "default", "0");
        this.fraction = new MessagingFraction();
        this.customizer = new RemoteConnectionSocketBindingCustomizer();

        this.customizer.group = this.group;
        this.customizer.fraction = fraction;
    }

    @Test
    public void testNoOp() {
        this.customizer.customize();
        assertThat( this.group.outboundSocketBindings() ).isEmpty();
    }

    @Test
    public void testSocketBinding() {

        this.fraction.defaultServer( (server)->{
            server.remoteConnection( "postoffice" );
        });

        this.customizer.customize();

        assertThat( this.group.outboundSocketBindings() ).hasSize(1);

        OutboundSocketBinding binding = this.group.outboundSocketBindings().get(0);

        assertThat( binding.name() ).isEqualTo( "postoffice" );
        assertThat( binding.remotePortExpression() ).isEqualTo(MessagingProperties.DEFAULT_REMOTE_PORT );
        assertThat( binding.remoteHostExpression() ).isEqualTo(MessagingProperties.DEFAULT_REMOTE_HOST );

    }

}
