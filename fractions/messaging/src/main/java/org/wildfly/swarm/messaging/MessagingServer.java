package org.wildfly.swarm.messaging;

import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.PathElement;
import org.jboss.dmr.ModelNode;

import java.util.ArrayList;
import java.util.List;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ADD;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP_ADDR;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.SUBSYSTEM;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.UNDEFINE_ATTRIBUTE_OPERATION;

/**
 * @author Bob McWhirter
 */
public class MessagingServer {

    private final List<ModelNode> list = new ArrayList<>();
    private final PathAddress address;

    public MessagingServer() {
        this("default");
    }

    public MessagingServer(String name) {
        this.address = PathAddress.pathAddress(PathElement.pathElement(SUBSYSTEM, "messaging")).append("hornetq-server", "default");

        ModelNode node = new ModelNode();
        node.get(OP_ADDR).set(this.address.toModelNode());
        node.get(OP).set(ADD);
        node.get("journal-file-size").set(102400L);
        this.list.add(node);
    }

    public MessagingServer enableInVmConnector() {
        return enableInVmConnector("java:/ConnectionFactory");
    }

    public MessagingServer enableInVmConnector(String jndiName) {
        ModelNode node = new ModelNode();
        node.get(OP_ADDR).set(this.address.append("in-vm-connector", "in-vm").toModelNode());
        node.get(OP).set(ADD);
        node.get("server-id").set(0);
        this.list.add(node);

        node = new ModelNode();
        node.get(OP_ADDR).set(this.address.append("in-vm-acceptor", "in-vm").toModelNode());
        node.get(OP).set(ADD);
        node.get("server-id").set(0);
        this.list.add(node);

        node = new ModelNode();
        node.get(OP_ADDR).set(this.address.append("connection-factory", "InVmConnectionFactory").toModelNode());
        node.get(OP).set(ADD);
        node.get("connector").set("in-vm", new ModelNode());
        node.get("entries").setEmptyList().add(jndiName);
        this.list.add(node);

        return this;
    }

    public MessagingServer topic(String name) {
        ModelNode node = new ModelNode();
        node.get(OP_ADDR).set(this.address.append("jms-topic", name).toModelNode());
        node.get(OP).set(ADD);
        node.get( "entries" ).setEmptyList().add("java:/jms/topic/" + name);
        this.list.add( node );
        return this;
    }

    public MessagingServer queue(String name) {
        ModelNode node = new ModelNode();
        node.get(OP_ADDR).set(this.address.append("jms-queue", name).toModelNode());
        node.get(OP).set(ADD);
        node.get( "entries" ).setEmptyList().add( "java:/jms/queue/" + name );
        this.list.add( node );
        return this;
    }

    public MessagingServer enableHttpConnector(String jndiName) {
        return this;
    }


    List<ModelNode> getList() {
        return this.list;
    }
}
