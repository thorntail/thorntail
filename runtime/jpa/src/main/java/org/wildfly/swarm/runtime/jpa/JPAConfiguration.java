package org.wildfly.swarm.runtime.jpa;

import java.util.ArrayList;
import java.util.List;

import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.PathElement;
import org.jboss.dmr.ModelNode;
import org.wildfly.swarm.jpa.JPAFraction;
import org.wildfly.swarm.runtime.container.AbstractServerConfiguration;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ADD;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.EXTENSION;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP_ADDR;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.SUBSYSTEM;

/**
 * @author Ken Finnigan
 */
public class JPAConfiguration extends AbstractServerConfiguration<JPAFraction> {

    public JPAConfiguration() {
        super(JPAFraction.class);
    }

    @Override
    public JPAFraction defaultFraction() {
        return new JPAFraction();
    }

    @Override
    public List<ModelNode> getList(JPAFraction fraction) {
        List<ModelNode> list = new ArrayList<>();

        PathAddress address = PathAddress.pathAddress(PathElement.pathElement(SUBSYSTEM, "jpa"));

        ModelNode node = new ModelNode();
        node.get(OP_ADDR).set(EXTENSION, "org.jboss.as.jpa");
        node.get(OP).set(ADD);
        list.add(node);

        node = new ModelNode();
        node.get(OP_ADDR).set(address.toModelNode());
        node.get(OP).set(ADD);
        node.get("default-datasource").set("");
        node.get("default-extended-persistence-inheritence").set("DEEP");
        list.add(node);

        return list;
    }
}
