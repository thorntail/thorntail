package org.wildfly.swarm.jaxrs.runtime;

import java.util.ArrayList;
import java.util.List;

import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.PathElement;
import org.jboss.dmr.ModelNode;
import org.wildfly.swarm.container.runtime.AbstractServerConfiguration;
import org.wildfly.swarm.jaxrs.JAXRSFraction;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ADD;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.EXTENSION;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP_ADDR;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.SUBSYSTEM;

/**
 * @author Bob McWhirter
 */
public class JAXRSConfiguration extends AbstractServerConfiguration<JAXRSFraction> {

    public JAXRSConfiguration() {
        super(JAXRSFraction.class);
    }

    @Override
    public JAXRSFraction defaultFraction() {
        return new JAXRSFraction();
    }

    @Override
    public List<ModelNode> getList(JAXRSFraction fraction) {
        List<ModelNode> list = new ArrayList<>();

        PathAddress address = PathAddress.pathAddress(PathElement.pathElement(SUBSYSTEM, "jaxrs"));

        ModelNode node = new ModelNode();
        node.get(OP_ADDR).set(EXTENSION, "org.jboss.as.jaxrs");
        node.get(OP).set(ADD);
        list.add(node);

        node = new ModelNode();
        node.get(OP_ADDR).set(address.toModelNode());
        node.get(OP).set(ADD);
        list.add(node);

        return list;

    }
}
