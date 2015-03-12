package org.wildfly.selfcontained.web;

import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.PathElement;
import org.jboss.dmr.ModelNode;
import org.wildfly.selfcontained.container.Subsystem;

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
public class NamingSubsystem implements Subsystem {

    private List<ModelNode> list = new ArrayList<>();

    public NamingSubsystem() {

        PathAddress address = PathAddress.pathAddress(PathElement.pathElement(SUBSYSTEM, "naming"));

        ModelNode node = new ModelNode();
        node.get(OP_ADDR).set(EXTENSION, "org.jboss.as.naming");
        node.get(OP).set(ADD);
        this.list.add(node);

        node = new ModelNode();
        node.get(OP_ADDR).set(address.toModelNode());
        node.get(OP).set(ADD);
        this.list.add(node);

        /*
        node = new ModelNode();
        node.get(OP_ADDR).set(address.append("service", "remote-naming").toModelNode());
        node.get(OP).set(ADD);
        this.list.add(node);
        */
    }

    public List<ModelNode> getList() {
        return this.list;
    }

}
