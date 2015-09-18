package org.wildfly.swarm.jpa.jpa;

import java.util.ArrayList;
import java.util.List;

import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.PathElement;
import org.jboss.dmr.ModelNode;
import org.wildfly.apigen.invocation.Marshaller;
import org.wildfly.swarm.container.runtime.AbstractServerConfiguration;
import org.wildfly.swarm.jpa.JPAFraction;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ADD;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.EXTENSION;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP_ADDR;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.SUBSYSTEM;

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

        JPAFraction fraction = new JPAFraction();
        fraction.defaultExtendedPersistenceInheritance("DEEP");
        return fraction;
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
