package org.wildfly.swarm.messaging.runtime;

import java.util.Optional;

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
        customizer.remotePort = Optional.of("61666");
        customizer.customize();

        assertThat( fraction.subresources().servers() ).hasSize(1);

        EnhancedServer server = (EnhancedServer) fraction.subresources().server("default");

        assertThat( server ).isNotNull();

        assertThat( server.remoteConnections() ).hasSize(1);

        RemoteConnection connection = server.remoteConnections().get(0);

        assertThat( connection.name() ).isEqualTo(MessagingProperties.DEFAULT_REMOTE_MQ_NAME );
        assertThat( connection.host() ).isEqualTo(MessagingProperties.DEFAULT_REMOTE_HOST );
        assertThat( connection.port() ).isEqualTo("61666");
        assertThat( connection.jndiName() ).isEqualTo(MessagingProperties.DEFAULT_REMOTE_JNDI_NAME );
    }

    @Test
    public void testIfHostSet() {
        customizer.remoteHost = Optional.of("mq.foo.com");
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
        customizer.jndiName = Optional.of("java:/jms/iliketacos");
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
        customizer.remoteMqName = Optional.of("postoffice");
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
        customizer.remote = Optional.of(true);
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
