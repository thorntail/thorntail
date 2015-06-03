package org.wildfly.swarm.messaging;

import java.util.ArrayList;
import java.util.List;

import org.wildfly.swarm.container.Fraction;

/**
 * @author Bob McWhirter
 */
public class MessagingFraction implements Fraction {

    private List<MessagingServer> servers = new ArrayList<>();

    public MessagingFraction() {

    }

    public MessagingFraction server(MessagingServer server) {
        this.servers.add(server);
        return this;
    }

    public List<MessagingServer> servers() {
        return this.servers;
    }
}
