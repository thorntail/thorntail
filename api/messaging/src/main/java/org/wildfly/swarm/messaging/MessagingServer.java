package org.wildfly.swarm.messaging;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Bob McWhirter
 */
public class MessagingServer {

    private static int SERVER_ID_COUNTER = 0;

    private static int NAME_COUNTER = 0;

    private final int serverId;

    private String name;

    private Set<String> topics = new HashSet<>();

    private Set<String> queues = new HashSet<>();

    private String inVmConnectorName;

    public MessagingServer() {
        this("server-" + (++NAME_COUNTER));
    }

    public MessagingServer(String name) {
        this.name = name;
        this.serverId = (++SERVER_ID_COUNTER);
    }

    public String name() {
        return this.name;
    }

    public int serverID() {
        return this.serverId;
    }

    public MessagingServer topic(String name) {
        this.topics.add(name);
        return this;
    }

    public Set<String> topics() {
        return this.topics;
    }

    public MessagingServer queue(String name) {
        this.queues.add(name);
        return this;
    }

    public Set<String> queues() {
        return this.queues;
    }

    public MessagingServer enableInVMConnector() {
        return enableInVMConnector("java:/ConnectionFactory");
    }

    public MessagingServer enableInVMConnector(String jndiName) {
        this.inVmConnectorName = jndiName;
        return this;
    }

    public String inVMConnectorJNDIName() {
        return this.inVmConnectorName;
    }

}
