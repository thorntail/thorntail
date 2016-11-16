package org.wildfly.swarm.messaging;

import org.wildfly.swarm.spi.api.Defaultable;

import static org.wildfly.swarm.messaging.MessagingProperties.DEFAULT_REMOTE_HOST;
import static org.wildfly.swarm.messaging.MessagingProperties.DEFAULT_REMOTE_MQ_NAME;
import static org.wildfly.swarm.messaging.MessagingProperties.DEFAULT_REMOTE_PORT;
import static org.wildfly.swarm.spi.api.Defaultable.integer;
import static org.wildfly.swarm.spi.api.Defaultable.string;

/**
 * Details for a remote message-queue connection.
 *
 * <p>Supports outboard ActiveMQ/Artemis servers.</p>
 *
 * @author Bob McWhirter
 */
public class RemoteConnection {

    /**
     * Configuration functional interface for container-supplied objects.
     */
    public interface Consumer extends java.util.function.Consumer<RemoteConnection> {
    }

    /**
     * Supplier functional interface for user-supplied object.
     */
    public interface Supplier extends java.util.function.Supplier<RemoteConnection> {
    }

    /**
     * Construct.
     *
     * @param name The name of the connection. Also used for {@link #jndiName}.
     */
    public RemoteConnection(String name) {
        this.name.set(name);
    }

    /**
     * Retrieve the name of the connection.
     *
     * @return The name.
     */
    public String name() {
        return this.name.get();
    }

    /**
     * Set the host (or host expression).
     *
     * @param host The host literal or expression.
     * @return This connection.
     */
    public RemoteConnection host(String host) {
        this.host.set(host);
        return this;
    }

    /**
     * Retrieve the host (or host expression).
     *
     * @return The host (or host expression).
     */
    public String host() {
        return this.host.get();
    }

    /**
     * Set the port (or port expression).
     *
     * @param port The port (or port expression).
     * @return This connectoin.
     */
    public RemoteConnection port(int port) {
        this.port.set(port);
        return this;
    }

    /**
     * Retrieve the port (or port expression).
     *
     * @return The port (or port expression).
     */
    public int port() {
        return this.port.get();
    }

    /**
     * Set the JNDI name for the associated connection factory.
     *
     * <p>If unset, defaults to <code>java:/jms/<b>name</b></code></p>.
     *
     * @param jndiName The explicit JNDI name to bind the connection factory.
     * @return This connection.
     * @see #name()
     */
    public RemoteConnection jndiName(String jndiName) {
        this.jndiName.set(jndiName);
        return this;
    }

    /**
     * Retrieve the JNDI name of the associated connection factory.
     *
     * @return The JNDI name of the associated connection factory.
     */
    public String jndiName() {
        return this.jndiName.get();
    }

    private final Defaultable<String> name = string(DEFAULT_REMOTE_MQ_NAME);

    private final Defaultable<String> host = string(DEFAULT_REMOTE_HOST);

    private final Defaultable<Integer> port = integer(DEFAULT_REMOTE_PORT);

    private final Defaultable<String> jndiName = string(() -> "java:/jms/" + name.get());


}
