package org.wildfly.swarm.messaging;

import java.util.function.Consumer;

import org.wildfly.swarm.spi.api.OutboundSocketBinding;

/** Details for a remote message-queue connection.
 *
 * <p>Supports outboard ActiveMQ/Artemis servers.</p>
 *
 * @author Bob McWhirter
 */
public class RemoteConnection {

    /** Configuration functional interface for container-supplied objects. */
    public interface Consumer extends java.util.function.Consumer<RemoteConnection> {
    }

    /** Supplier functional interface for user-supplied object. */
    public interface Supplier extends java.util.function.Supplier<RemoteConnection> {
    }

    /** Construct.
     *
     * @param name The name of the connection. Also used for {@link #jndiName}.
     */
    public RemoteConnection(String name) {
        this.name = name;
    }

    /** Retrieve the name of the connection.
     *
     * @return The name.
     */
    public String name() {
        return this.name;
    }

    /** Set the host (or host expression).
     *
     * @param host The host literal or expression.
     * @return This connection.
     */
    public RemoteConnection host(String host) {
        this.host = host;
        return this;
    }

    /** Retrieve the host (or host expression).
     *
     * @return The host (or host expression).
     */
    public String host() {
        return this.host;
    }

    /** Set the port.
     *
     * @param port The port.
     * @return This connection.
     */
    public RemoteConnection port(int port) {
        this.port = "" + port;
        return this;
    }

    /** Set the port (or port expression).
     *
     * @param port The port (or port expression).
     * @return This connectoin.
     */
    public RemoteConnection port(String port) {
        this.port = port;
        return this;
    }

    /** Retrieve the port (or port expression).
     *
     * @return The port (or port expression).
     */
    public String port() {
        return this.port;
    }

    /** Set the JNDI name for the associated connection factory.
     *
     * <p>If unset, defaults to <code>java:/jms/<b>name</b></code></p>.
     *
     * @see #name()
     *
     * @param jndiName The explicit JNDI name to bind the connection factory.
     * @return This connection.
     */
    public RemoteConnection jndiName(String jndiName) {
        this.jndiName = jndiName;
        return this;
    }

    /** Retrieve the JNDI name of the associated connection factory.
     *
     * @return The JNDI name of the associated connection factory.
     */
    public String jndiName() {
        if ( jndiName != null ) {
            return this.jndiName;
        }

        return "java:/jms/" + this.name;
    }

    private String name;
    private String host = MessagingProperties.DEFAULT_REMOTE_HOST;
    private String port = MessagingProperties.DEFAULT_REMOTE_PORT;
    private String jndiName;


}
