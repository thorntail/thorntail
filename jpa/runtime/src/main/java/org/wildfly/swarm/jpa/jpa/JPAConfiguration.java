package org.wildfly.swarm.jpa.jpa;

import org.jboss.dmr.ModelNode;
import org.wildfly.config.invocation.Marshaller;
import org.wildfly.swarm.container.runtime.AbstractServerConfiguration;
import org.wildfly.swarm.jpa.JPAFraction;

import java.util.ArrayList;
import java.util.List;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.*;

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
    public List<ModelNode> getList(JPAFraction fraction) {
        List<ModelNode> list = new ArrayList<>();
        ModelNode node = new ModelNode();
        node.get(OP_ADDR).set(EXTENSION, "org.jboss.as.jpa");
        node.get(OP).set(ADD);
        list.add(node);
        try {
            list.addAll(Marshaller.marshal(fraction));
        } catch (Exception e) {
            System.err.println("Cannot configure JPA subsystem. " + e);
        }
        return list;
    }
}
