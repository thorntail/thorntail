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
public class RemoteConnectionInstallingCustomizerTest {

    private RemoteConnectionInstallingCustomizer customizer;
    private MessagingFraction fraction;

    @Before
    public void setUp() {
        this.customizer = new RemoteConnectionInstallingCustomizer();
        this.fraction = new MessagingFraction();

        this.customizer.fraction = fraction;
    }

    @Test
    public void testNoOpIfNoConfigSettings() {
        customizer.customize();
        assertThat( fraction.subresources().servers() ).isEmpty();
    }

    @Test
    public void testIfPortSet() {
        customizer.port.set( 61666 );
        customizer.customize();

        assertThat( fraction.subresources().servers() ).hasSize(1);

        EnhancedServer server = (EnhancedServer) fraction.subresources().server("default");

        assertThat( server ).isNotNull();

        assertThat( server.remoteConnections() ).hasSize(1);

        RemoteConnection connection = server.remoteConnections().get(0);

        assertThat( connection.name() ).isEqualTo(MessagingProperties.DEFAULT_REMOTE_MQ_NAME );
        assertThat( connection.host() ).isEqualTo(MessagingProperties.DEFAULT_REMOTE_HOST );
        assertThat( connection.port() ).isEqualTo(61666);
        assertThat( connection.jndiName() ).isEqualTo(MessagingProperties.DEFAULT_REMOTE_JNDI_NAME );
    }

    @Test
    public void testIfHostSet() {
        customizer.host.set( "mq.foo.com" );
        customizer.customize();

        assertThat( fraction.subresources().servers() ).hasSize(1);

        EnhancedServer server = (EnhancedServer) fraction.subresources().server("default");

        assertThat( server ).isNotNull();

        assertThat( server.remoteConnections() ).hasSize(1);

        RemoteConnection connection = server.remoteConnections().get(0);

        assertThat( connection.name() ).isEqualTo(MessagingProperties.DEFAULT_REMOTE_MQ_NAME );
        assertThat( connection.host() ).isEqualTo("mq.foo.com");
        assertThat( connection.port() ).isEqualTo(MessagingProperties.DEFAULT_REMOTE_PORT);
        assertThat( connection.jndiName() ).isEqualTo(MessagingProperties.DEFAULT_REMOTE_JNDI_NAME );
    }

    @Test
    public void testIfJndiNameSet() {
        customizer.jndiName.set("java:/jms/iliketacos");
        customizer.customize();

        assertThat( fraction.subresources().servers() ).hasSize(1);

        EnhancedServer server = (EnhancedServer) fraction.subresources().server("default");

        assertThat( server ).isNotNull();

        assertThat( server.remoteConnections() ).hasSize(1);

        RemoteConnection connection = server.remoteConnections().get(0);

        assertThat( connection.name() ).isEqualTo(MessagingProperties.DEFAULT_REMOTE_MQ_NAME );
        assertThat( connection.host() ).isEqualTo(MessagingProperties.DEFAULT_REMOTE_HOST);
        assertThat( connection.port() ).isEqualTo(MessagingProperties.DEFAULT_REMOTE_PORT);
        assertThat( connection.jndiName() ).isEqualTo("java:/jms/iliketacos");
    }

    @Test
    public void testIfMqNameSet() {
        customizer.name.set("postoffice" );
        customizer.customize();

        assertThat( fraction.subresources().servers() ).hasSize(1);

        EnhancedServer server = (EnhancedServer) fraction.subresources().server("default");

        assertThat( server ).isNotNull();

        assertThat( server.remoteConnections() ).hasSize(1);

        RemoteConnection connection = server.remoteConnections().get(0);

        assertThat( connection.name() ).isEqualTo("postoffice");
        assertThat( connection.host() ).isEqualTo(MessagingProperties.DEFAULT_REMOTE_HOST);
        assertThat( connection.port() ).isEqualTo(MessagingProperties.DEFAULT_REMOTE_PORT);
        assertThat( connection.jndiName() ).isEqualTo("java:/jms/postoffice");
    }

    @Test
    public void testIfRemoteFlagSet() {
        customizer.enabled.set(true);
        customizer.customize();

        assertThat( fraction.subresources().servers() ).hasSize(1);

        EnhancedServer server = (EnhancedServer) fraction.subresources().server("default");

        assertThat( server ).isNotNull();

        assertThat( server.remoteConnections() ).hasSize(1);

        RemoteConnection connection = server.remoteConnections().get(0);

        assertThat( connection.name() ).isEqualTo(MessagingProperties.DEFAULT_REMOTE_MQ_NAME );
        assertThat( connection.host() ).isEqualTo(MessagingProperties.DEFAULT_REMOTE_HOST);
        assertThat( connection.port() ).isEqualTo(MessagingProperties.DEFAULT_REMOTE_PORT);
        assertThat( connection.jndiName() ).isEqualTo(MessagingProperties.DEFAULT_REMOTE_JNDI_NAME);
    }
}
