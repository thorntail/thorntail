package org.wildfly.swarm.runtime.io;

import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.PathElement;
import org.jboss.dmr.ModelNode;
import org.wildfly.swarm.io.IOFraction;
import org.wildfly.swarm.runtime.container.AbstractServerConfiguration;

import java.util.ArrayList;
import java.util.List;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ADD;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.EXTENSION;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP_ADDR;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.SUBSYSTEM;

/**
 * @author Bob McWhirter
 */
public class IOConfiguration extends AbstractServerConfiguration<IOFraction> {

    public IOConfiguration() {
        super(IOFraction.class);
    }

    @Override
    public IOFraction defaultFraction() {
        return new IOFraction();
    }

    @Override
    public List<ModelNode> getList(IOFraction fraction) {
        List<ModelNode> list = new ArrayList<>();

        PathAddress address = PathAddress.pathAddress(PathElement.pathElement(SUBSYSTEM, "io"));

        ModelNode node = new ModelNode();
        node.get(OP_ADDR).set(EXTENSION, "org.wildfly.extension.io");
        node.get(OP).set(ADD);
        list.add(node);

        node = new ModelNode();
        node.get(OP_ADDR).set(address.toModelNode());
        node.get(OP).set(ADD);
        list.add(node);

        node = new ModelNode();
        node.get(OP_ADDR).set(address.append("worker", "default").toModelNode() );
        node.get(OP).set(ADD);
        list.add(node);

        node = new ModelNode();
        node.get(OP_ADDR).set(address.append("buffer-pool", "default").toModelNode() );
        node.get(OP).set(ADD);
        list.add(node);


        return list;

    }
}
