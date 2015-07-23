package org.wildfly.swarm.runtime.clustering;

import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.PathElement;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.ValueExpression;
import org.wildfly.swarm.clustering.*;
import org.wildfly.swarm.runtime.container.AbstractServerConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.*;

/**
 * @author Bob McWhirter
 */
public class ClusteringConfiguration extends AbstractServerConfiguration<ClusteringFraction> {

    public ClusteringConfiguration() {
        super(ClusteringFraction.class);
    }

    @Override
    public ClusteringFraction defaultFraction() {
        return new ClusteringFraction()
                .defaultChannel(new Channel("ee"))
                .defaultStack(Stack.defaultUDPStack());
    }

    @Override
    public List<ModelNode> getList(ClusteringFraction fraction) {
        List<ModelNode> list = new ArrayList<>();

        ModelNode node = new ModelNode();
        node.get(OP_ADDR).set(EXTENSION, "org.jboss.as.clustering.jgroups");
        node.get(OP).set(ADD);
        list.add(node);

        PathAddress address = PathAddress.pathAddress(PathElement.pathElement(SUBSYSTEM, "jgroups"));

        node = new ModelNode();
        node.get(OP_ADDR).set(address.toModelNode());
        node.get(OP).set(ADD);
        if (fraction.defaultChannel() != null) {
            node.get("default-channel").set(fraction.defaultChannel().name());
        }
        if (fraction.defaultStack() != null) {
            node.get("default-stack").set(fraction.defaultStack().name());
        }
        list.add(node);

        for (Channel channel : fraction.channels()) {
            node = new ModelNode();
            node.get(OP_ADDR).set(address.append("channel", channel.name()).toModelNode());
            node.get(OP).set(ADD);
            list.add(node);
        }

        for (Stack stack : fraction.stacks()) {
            PathAddress stackAddr = address.append("stack", stack.name());

            node = new ModelNode();
            node.get(OP_ADDR).set(stackAddr.toModelNode());
            node.get(OP).set(ADD);
            list.add(node);

            node = new ModelNode();
            node.get(OP_ADDR).set(stackAddr.append("transport", stack.transport().name()).toModelNode());
            node.get(OP).set(ADD);
            node.get("socket-binding").set(stack.transport().socketBinding());
            for (Map.Entry<String, String> entry : stack.transport().properties().entrySet()) {
                node.get(entry.getKey()).set(new ValueExpression(entry.getValue()));
            }

            list.add(node);

            for (Protocol protocol : stack.protocols()) {
                node = new ModelNode();
                node.get(OP_ADDR).set(stackAddr.append("protocol", protocol.name()).toModelNode());
                node.get(OP).set(ADD);
                if (protocol instanceof SocketBindingProtocol) {
                    node.get(SOCKET_BINDING).set(((SocketBindingProtocol) protocol).socketBinding());
                }
                for (Map.Entry<String, String> entry : protocol.properties().entrySet()) {
                    node.get(entry.getKey()).set(new ValueExpression(entry.getValue()));
                }
                list.add(node);
            }
        }

        /*
        node = new ModelNode();
        node.get(OP_ADDR).set(address.append("stack", "tcp").toModelNode());
        node.get(OP).set(ADD);
        list.add(node);

        node = new ModelNode();
        node.get(OP_ADDR).set(address.append("stack", "tcp").append("transport", "TCP").toModelNode());
        node.get(OP).set(ADD);
        node.get(SOCKET_BINDING).set("jgroups-tcp");
        list.add(node);

        node = new ModelNode();
        node.get(OP_ADDR).set(address.append("stack", "tcp").append("protocol", "MPING").toModelNode());
        node.get(OP).set(ADD);
        node.get(SOCKET_BINDING).set("jgroups-mping");
        list.add(node);

        node = new ModelNode();
        node.get(OP_ADDR).set(address.append("stack", "tcp").append("protocol", "MERGE3").toModelNode());
        node.get(OP).set(ADD);
        list.add(node);

        node = new ModelNode();
        node.get(OP_ADDR).set(address.append("stack", "tcp").append("protocol", "FD_SOCK").toModelNode());
        node.get(OP).set(ADD);
        node.get(SOCKET_BINDING).set("jgroups-tcp-fd");
        list.add(node);

        node = new ModelNode();
        node.get(OP_ADDR).set(address.append("stack", "tcp").append("protocol", "FD").toModelNode());
        node.get(OP).set(ADD);
        list.add(node);

        node = new ModelNode();
        node.get(OP_ADDR).set(address.append("stack", "tcp").append("protocol", "VERIFY_SUSPECT").toModelNode());
        node.get(OP).set(ADD);
        list.add(node);

        node = new ModelNode();
        node.get(OP_ADDR).set(address.append("stack", "tcp").append("protocol", "pbcast.NAKACK2").toModelNode());
        node.get(OP).set(ADD);
        list.add(node);

        node = new ModelNode();
        node.get(OP_ADDR).set(address.append("stack", "tcp").append("protocol", "UNICAST3").toModelNode());
        node.get(OP).set(ADD);
        list.add(node);

        node = new ModelNode();
        node.get(OP_ADDR).set(address.append("stack", "tcp").append("protocol", "pbcast.STABLE").toModelNode());
        node.get(OP).set(ADD);
        list.add(node);

        node = new ModelNode();
        node.get(OP_ADDR).set(address.append("stack", "tcp").append("protocol", "pbcast.GMS").toModelNode());
        node.get(OP).set(ADD);
        list.add(node);

        node = new ModelNode();
        node.get(OP_ADDR).set(address.append("stack", "tcp").append("protocol", "MFC").toModelNode());
        node.get(OP).set(ADD);
        list.add(node);

        node = new ModelNode();
        node.get(OP_ADDR).set(address.append("stack", "tcp").append("protocol", "FRAG2").toModelNode());
        node.get(OP).set(ADD);
        list.add(node);

        node = new ModelNode();
        node.get(OP_ADDR).set(address.append("stack", "tcp").append("protocol", "RSVP").toModelNode());
        node.get(OP).set(ADD);
        list.add(node);

*/

        return list;

    }
}
