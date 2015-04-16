package org.wildfly.swarm.io;

import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.PathElement;
import org.jboss.dmr.ModelNode;
import org.wildfly.swarm.container.Subsystem;

import java.util.ArrayList;
import java.util.List;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ADD;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.EXTENSION;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP_ADDR;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.SUBSYSTEM;

/**

/**
 * @author Bob McWhirter
 */
public class IoSubsystem implements Subsystem {
    private List<ModelNode> list = new ArrayList<>();

    public IoSubsystem() {

        PathAddress address = PathAddress.pathAddress(PathElement.pathElement(SUBSYSTEM, "io"));

        ModelNode node = new ModelNode();
        node.get(OP_ADDR).set(EXTENSION, "org.wildfly.extension.io");
        node.get(OP).set(ADD);
        this.list.add(node);

        node = new ModelNode();
        node.get(OP_ADDR).set(address.toModelNode());
        node.get(OP).set(ADD);
        this.list.add(node);

        node = new ModelNode();
        node.get(OP_ADDR).set(address.append("worker", "default").toModelNode() );
        node.get(OP).set(ADD);
        this.list.add(node);

        node = new ModelNode();
        node.get(OP_ADDR).set(address.append("buffer-pool", "default").toModelNode() );
        node.get(OP).set(ADD);
        this.list.add(node);




    }

    @Override
    public List<ModelNode> getList() {
        return this.list;
    }
}
