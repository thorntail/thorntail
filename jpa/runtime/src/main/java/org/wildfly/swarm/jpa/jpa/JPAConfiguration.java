package org.wildfly.swarm.jpa.jpa;

import java.util.ArrayList;
import java.util.List;

import org.jboss.dmr.ModelNode;
import org.wildfly.swarm.config.runtime.invocation.Marshaller;
import org.wildfly.swarm.container.runtime.AbstractServerConfiguration;
import org.wildfly.swarm.jpa.JPAFraction;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ADD;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.EXTENSION;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP_ADDR;

/**
 * @author Ken Finnigan
 * @author Lance Ball
 */
public class JPAConfiguration extends AbstractServerConfiguration<JPAFraction> {

    public JPAConfiguration() {
        super(JPAFraction.class);
    }

    @Override
    public JPAFraction defaultFraction() {
        return JPAFraction.createDefaultFraction();
    }

    @Override
    public List<ModelNode> getList(JPAFraction fraction) throws Exception {
        List<ModelNode> list = new ArrayList<>();
        ModelNode node = new ModelNode();
        node.get(OP_ADDR).set(EXTENSION, "org.jboss.as.jpa");
        node.get(OP).set(ADD);
        list.add(node);

        list.addAll(Marshaller.marshal(fraction));

        return list;
    }
}
