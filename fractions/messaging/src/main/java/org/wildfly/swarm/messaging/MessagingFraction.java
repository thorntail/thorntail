package org.wildfly.swarm.messaging;

import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.PathElement;
import org.jboss.dmr.ModelNode;
import org.wildfly.swarm.container.AbstractFraction;

import java.util.ArrayList;
import java.util.List;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ADD;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.EXTENSION;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP_ADDR;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.SERVER_IDENTITY;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.SOCKET_BINDING;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.SUBSYSTEM;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.VALUE;

/**
 * @author Bob McWhirter
 */
public class MessagingFraction extends AbstractFraction {

    private List<ModelNode> list = new ArrayList<>();

    private List<MessagingServer> servers = new ArrayList<>();

    public MessagingFraction() {

        PathAddress address = PathAddress.pathAddress(PathElement.pathElement(SUBSYSTEM, "messaging"));

        ModelNode node = new ModelNode();
        node.get(OP_ADDR).set(EXTENSION, "org.jboss.as.messaging");
        node.get(OP).set(ADD);
        this.list.add(node);

        node = new ModelNode();
        node.get(OP_ADDR).set(address.toModelNode());
        node.get(OP).set(ADD);
        this.list.add(node);
    }

    public MessagingFraction server(MessagingServer server) {
        this.servers.add( server );
        return this;
    }

    @Override
    public List<ModelNode> getList() {
        for ( MessagingServer each : this.servers ) {
            this.list.addAll( each.getList() );
        }
        return this.list;
    }
}
