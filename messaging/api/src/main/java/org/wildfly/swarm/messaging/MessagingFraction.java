package org.wildfly.swarm.messaging;

import org.wildfly.swarm.config.MessagingActivemq;
import org.wildfly.swarm.config.messaging_activemq.JmsBridge;
import org.wildfly.swarm.config.messaging_activemq.Server;
import org.wildfly.swarm.config.messaging_activemq.server.InVmAcceptor;
import org.wildfly.swarm.container.Fraction;

import java.util.List;

/**
 * @author Bob McWhirter
 * @author Lance Ball
 */
public class MessagingFraction extends MessagingActivemq<MessagingFraction> implements Fraction {

    private MessagingFraction() {}

    @Override
    public MessagingFraction server(Server server) {
        return super.server(server);
    }

    @Override
    public MessagingFraction jmsBridges(List<JmsBridge> value) {
        return super.jmsBridges(value);
    }

    @Override
    public MessagingFraction jmsBridge(JmsBridge value) {
        return super.jmsBridge(value);
    }

    @Override
    public MessagingFraction servers(List<Server> value) {
        return super.servers(value);
    }

    public static MessagingFraction createDefaultFraction() {
        MessagingFraction fraction = new MessagingFraction();
        return fraction;

    }
}
