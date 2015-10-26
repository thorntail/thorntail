package org.wildfly.swarm.messaging;

import java.util.Arrays;

import org.wildfly.swarm.config.MessagingActiveMQ;
import org.wildfly.swarm.config.messaging_activemq.server.ConnectionFactory;
import org.wildfly.swarm.config.messaging_activemq.server.PooledConnectionFactory;
import org.wildfly.swarm.container.Fraction;

/**
 * @author Bob McWhirter
 * @author Lance Ball
 */
public class MessagingFraction extends MessagingActiveMQ<MessagingFraction> implements Fraction {

    private MessagingFraction() {
    }

    public static MessagingFraction createDefaultFraction() {
        return new MessagingFraction();
    }

    public MessagingFraction server(String childKey, ServerConfigurator config) {
        Server s = new Server(childKey);
        config.configure(s);
        return server(s);
    }

    public MessagingFraction defaultServer(ServerConfigurator config) {
        server("default", (s) -> {
            s.enableInVm();
            config.configure(s);
        });
        return this;
    }
}
