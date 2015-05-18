package org.wildfly.swarm.runtime.naming;

import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.PathElement;
import org.jboss.dmr.ModelNode;
import org.wildfly.swarm.naming.NamingFraction;
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
public class NamingConfiguration extends AbstractServerConfiguration<NamingFraction> {

    public NamingConfiguration() {
        super(NamingFraction.class);
    }

    @Override
    public NamingFraction defaultFraction() {
        return new NamingFraction();
    }

    @Override
    public List<ModelNode> getList(NamingFraction fraction) {
        List<ModelNode> list = new ArrayList<>();

        PathAddress address = PathAddress.pathAddress(PathElement.pathElement(SUBSYSTEM, "naming"));

        ModelNode node = new ModelNode();
        node.get(OP_ADDR).set(EXTENSION, "org.jboss.as.naming");
        node.get(OP).set(ADD);
        list.add(node);

        node = new ModelNode();
        node.get(OP_ADDR).set(address.toModelNode());
        node.get(OP).set(ADD);
        list.add(node);

        return list;

    }
}
