package org.wildfly.swarm.messaging.runtime;

import org.junit.Before;
import org.junit.Test;
import org.wildfly.swarm.config.messaging.activemq.RemoteConnector;
import org.wildfly.swarm.config.messaging.activemq.server.PooledConnectionFactory;
import org.wildfly.swarm.messaging.EnhancedServer;
import org.wildfly.swarm.messaging.MessagingFraction;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author Bob McWhirter
 */
public class RemoteConnectionCustomizerTest {

    private RemoteConnectionCustomizer customizer;

    private MessagingFraction fraction;

    @Before
    public void setUp() {
        this.customizer = new RemoteConnectionCustomizer();
        this.fraction = new MessagingFraction();

        this.customizer.fraction = fraction;
    }

    @Test
    public void testIfNoRemoteConnections() {
        this.customizer.customize();
        assertThat( this.fraction.subresources().servers()).isEmpty();
    }

    @Test
    public void testIfRemoteConnection() {
        this.fraction.server( "default", (server)->{
            server.remoteConnection( "postoffice" );
        });

        this.customizer.customize();

        EnhancedServer server = (EnhancedServer) this.fraction.subresources().servers().get(0);

        assertThat( server.remoteConnections()).hasSize(1);
        assertThat( server.subresources().pooledConnectionFactories()).hasSize(1);

        PooledConnectionFactory connectionFactory = server.subresources().pooledConnectionFactories().get(0);

        assertThat( connectionFactory.getKey() ).isEqualTo( "postoffice" );
        assertThat( connectionFactory.entries() ).containsExactly( "java:/jms/postoffice" );

        assertThat( server.subresources().remoteConnectors() ).hasSize(1);
        RemoteConnector remoteConnector = server.subresources().remoteConnectors().get(0);

        assertThat( remoteConnector.getKey() ).isEqualTo( "postoffice" );
        assertThat( remoteConnector.socketBinding() ).isEqualTo( "postoffice" );
    }
}
