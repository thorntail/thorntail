package org.wildfly.swarm.container.runtime;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.jboss.as.controller.PathAddress;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.ValueExpression;
import org.wildfly.swarm.spi.api.OutboundSocketBinding;
import org.wildfly.swarm.spi.api.SocketBinding;
import org.wildfly.swarm.spi.api.SocketBindingGroup;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ADD;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.DEFAULT_INTERFACE;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.HOST;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.MULTICAST_ADDRESS;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.MULTICAST_PORT;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP_ADDR;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.PORT;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.PORT_OFFSET;

/**
 * @author Bob McWhirter
 */
public class SocketBindingGroupMarshaller {

    @Inject
    @Any
    private Instance<SocketBindingGroup> socketBindingGroups;

    public List<ModelNode> marshall() {
        List<ModelNode> list = new ArrayList<>();
        for (SocketBindingGroup group : this.socketBindingGroups) {
            System.err.println("CONFIGURE SOCKET BINDING GROUP: " + group);
            PathAddress address = PathAddress.pathAddress("socket-binding-group", group.name());
            ModelNode node = new ModelNode();
            node.get(OP).set(ADD);
            node.get(OP_ADDR).set(address.toModelNode());
            node.get(DEFAULT_INTERFACE).set(group.defaultInterface());
            node.get(PORT_OFFSET).set(new ValueExpression(group.portOffsetExpression()));
            list.add(node);

            for (SocketBinding binding : group.socketBindings()) {
                System.err.println("CONFIGURE SOCKET BINDING: " + binding);
                configureSocketBinding(address, binding, list);
            }

            for (OutboundSocketBinding binding : group.outboundSocketBindings()) {
                System.err.println("CONFIGURE OUTBOUND SOCKET BINDING: " + binding);
                configureSocketBinding(address, binding, list);
            }
        }

        return list;
    }

    private void configureSocketBindings(PathAddress address, SocketBindingGroup group, List<ModelNode> list) {
        List<SocketBinding> socketBindings = group.socketBindings();

        for (SocketBinding each : socketBindings) {
            configureSocketBinding(address, each, list);
        }

        List<OutboundSocketBinding> outboundSocketBindings = group.outboundSocketBindings();

        for (OutboundSocketBinding each : outboundSocketBindings) {
            configureSocketBinding(address, each, list);
        }
    }

    private void configureSocketBinding(PathAddress address, SocketBinding binding, List<ModelNode> list) {


        ModelNode node = new ModelNode();

        node.get(OP_ADDR).set(address.append("socket-binding", binding.name()).toModelNode());
        node.get(OP).set(ADD);
        node.get(PORT).set(new ValueExpression(binding.portExpression()));
        if (binding.multicastAddress() != null) {
            node.get(MULTICAST_ADDRESS).set(binding.multicastAddress());
        }
        if (binding.multicastPortExpression() != null) {
            node.get(MULTICAST_PORT).set(new ValueExpression(binding.multicastPortExpression()));
        }

        list.add(node);
    }

    private void configureSocketBinding(PathAddress address, OutboundSocketBinding binding, List<ModelNode> list) {

        ModelNode node = new ModelNode();

        node.get(OP_ADDR).set(address.append("remote-destination-outbound-socket-binding", binding.name()).toModelNode());
        node.get(OP).set(ADD);
        node.get(HOST).set(new ValueExpression(binding.remoteHostExpression()));
        node.get(PORT).set(new ValueExpression(binding.remotePortExpression()));

        list.add(node);
    }


}
