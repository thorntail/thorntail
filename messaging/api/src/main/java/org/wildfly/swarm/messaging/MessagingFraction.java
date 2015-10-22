package org.wildfly.swarm.messaging;

import org.wildfly.swarm.config.MessagingActivemq;
import org.wildfly.swarm.config.messaging_activemq.Server;
import org.wildfly.swarm.container.Fraction;

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

    public static MessagingFraction createDefaultFraction() {
        return new MessagingFraction();
    }
}
