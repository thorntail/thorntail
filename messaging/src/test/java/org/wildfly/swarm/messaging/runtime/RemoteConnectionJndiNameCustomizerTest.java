package org.wildfly.swarm.messaging.runtime;

import org.junit.Before;
import org.junit.Test;
import org.wildfly.swarm.messaging.EnhancedServer;
import org.wildfly.swarm.messaging.MessagingFraction;
import org.wildfly.swarm.messaging.MessagingProperties;
import org.wildfly.swarm.messaging.RemoteConnection;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author Bob McWhirter
 */
public class RemoteConnectionJndiNameCustomizerTest {

    private RemoteConnectionJndiNameCustomizer customizer;
    private MessagingFraction fraction;

    @Before
    public void setUp() {
        this.customizer = new RemoteConnectionJndiNameCustomizer();
        this.fraction = new MessagingFraction();

        this.customizer.fraction = this.fraction;
    }

    @Test
    public void testNoOpCustomization() {
        this.fraction.defaultServer( (server)->{
            server.remoteConnection();
        });

        this.customizer.customize();

        EnhancedServer server = (EnhancedServer) this.fraction.subresources().server("default");
        assertThat( server ).isNotNull();

        assertThat( server.remoteConnections() ).hasSize(1);

        RemoteConnection connection = server.remoteConnections().get(0);

        assertThat( connection.jndiName() ).isEqualTo(MessagingProperties.DEFAULT_REMOTE_JNDI_NAME );
    }

    @Test
    public void testWithCustomization() {
        this.fraction.defaultServer( (server)->{
            server.remoteConnection();
            server.remoteConnection( "other-mq" );
        });

        this.customizer.jndiName = "java:/jms/tacos";

        this.customizer.customize();

        EnhancedServer server = (EnhancedServer) this.fraction.subresources().server("default");
        assertThat( server ).isNotNull();

        assertThat( server.remoteConnections() ).hasSize(2);

        RemoteConnection connection = server.remoteConnections().get(0);
        RemoteConnection otherConnection = server.remoteConnections().get(1);

        assertThat( connection.jndiName() ).isEqualTo("java:/jms/tacos");
        assertThat( otherConnection.jndiName() ).isEqualTo("java:/jms/other-mq" );
    }
}
