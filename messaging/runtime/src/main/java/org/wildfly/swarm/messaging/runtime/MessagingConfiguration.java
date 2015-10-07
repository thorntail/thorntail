package org.wildfly.swarm.messaging.runtime;

import java.util.ArrayList;
import java.util.List;

import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.PathElement;
import org.jboss.dmr.ModelNode;
import org.jboss.shrinkwrap.api.Archive;
import org.wildfly.swarm.container.JARArchive;
import org.wildfly.swarm.container.runtime.AbstractServerConfiguration;
import org.wildfly.swarm.messaging.MessagingFraction;
import org.wildfly.swarm.messaging.MessagingServer;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ADD;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.EXTENSION;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP_ADDR;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.SUBSYSTEM;

/**
 * @author Bob McWhirter
 */
public class MessagingConfiguration extends AbstractServerConfiguration<MessagingFraction> {

    private PathAddress address = PathAddress.pathAddress(PathElement.pathElement(SUBSYSTEM, "messaging-activemq"));

    public MessagingConfiguration() {
        super(MessagingFraction.class);
    }

    @Override
    public MessagingFraction defaultFraction() {
        return new MessagingFraction();
    }

    @Override
    public void prepareArchive(Archive a) {
        a.as(JARArchive.class).addModule("javax.jms.api");
    }

    @Override
    public List<ModelNode> getList(MessagingFraction fraction) {
        List<ModelNode> list = new ArrayList<>();

        ModelNode node = new ModelNode();
        node.get(OP_ADDR).set(EXTENSION, "org.wildfly.extension.messaging-activemq");
        node.get(OP).set(ADD);
        list.add(node);

        node = new ModelNode();
        node.get(OP_ADDR).set(address.toModelNode());
        node.get(OP).set(ADD);
        list.add(node);

        addServers(fraction, list);

        return list;
    }

    protected void addServers(MessagingFraction fraction, List<ModelNode> list) {
        List<MessagingServer> servers = fraction.servers();

        for (MessagingServer each : servers) {
            addServer(each, list);
        }
    }

    protected void addServer(MessagingServer server, List<ModelNode> list) {
        PathAddress serverAddress = this.address.append("server", server.name());


        ModelNode node = new ModelNode();
        node.get(OP_ADDR).set(serverAddress.toModelNode());
        node.get(OP).set(ADD);
        node.get("journal-file-size").set(102400L);
        list.add(node);

        if (server.inVMConnectorJNDIName() != null) {
            node = new ModelNode();
            node.get(OP_ADDR).set(serverAddress.append("in-vm-connector", "in-vm").toModelNode());
            node.get(OP).set(ADD);
            node.get("server-id").set(server.serverID());
            list.add(node);

            node = new ModelNode();
            node.get(OP_ADDR).set(serverAddress.append("in-vm-acceptor", "in-vm").toModelNode());
            node.get(OP).set(ADD);
            node.get("server-id").set(server.serverID());
            list.add(node);

            node = new ModelNode();
            node.get(OP_ADDR).set(serverAddress.append("connection-factory", "InVmConnectionFactory").toModelNode());
            node.get(OP).set(ADD);
            node.get("connectors").setEmptyList().add("in-vm");
            node.get("entries").setEmptyList().add(server.inVMConnectorJNDIName());
            list.add(node);
        }

        addTopics(server, list);
        addQueues(server, list);

        addConnectionFactory(server, list);
    }

    protected void addTopics(MessagingServer server, List<ModelNode> list) {
        PathAddress serverAddress = this.address.append("server", server.name());

        for (String each : server.topics()) {
            ModelNode node = new ModelNode();
            node.get(OP_ADDR).set(serverAddress.append("jms-topic", each).toModelNode());
            node.get(OP).set(ADD);
            node.get("entries").setEmptyList().add("java:/jms/topic/" + each);
            list.add(node);
        }
    }

    protected void addQueues(MessagingServer server, List<ModelNode> list) {
        PathAddress serverAddress = this.address.append("server", server.name());

        for (String each : server.queues()) {
            ModelNode node = new ModelNode();
            node.get(OP_ADDR).set(serverAddress.append("jms-queue", each).toModelNode());
            node.get(OP).set(ADD);
            node.get("entries").setEmptyList().add("java:/jms/queue/" + each);
            list.add(node);
        }
    }

    protected void addConnectionFactory(MessagingServer server, List<ModelNode> list) {
        PathAddress serverAddress = this.address.append("server", server.name());

        ModelNode node = new ModelNode();
        node.get(OP_ADDR).set(serverAddress.append("pooled-connection-factory", "activemq-ra").toModelNode());
        node.get(OP).set(ADD);
        node.get("transaction").set("xa");
        node.get("connectors").add("in-vm");
        node.get("entries").add("java:/JmsXA");
        node.get("entries").add("java:jboss/DefaultJMSConnectionFactory");

        list.add( node );
    }
}
