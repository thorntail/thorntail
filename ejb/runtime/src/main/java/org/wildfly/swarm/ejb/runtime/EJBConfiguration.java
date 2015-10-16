package org.wildfly.swarm.ejb.runtime;

import org.jboss.dmr.ModelNode;
import org.wildfly.swarm.config.runtime.invocation.Marshaller;
import org.wildfly.swarm.container.runtime.AbstractServerConfiguration;
import org.wildfly.swarm.ejb.EJBFraction;

import java.util.ArrayList;
import java.util.List;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.*;

/**
 * @author Ken Finnigan
 */
public class EJBConfiguration extends AbstractServerConfiguration<EJBFraction> {

    public EJBConfiguration() {
        super(EJBFraction.class);
    }

    @Override
    public EJBFraction defaultFraction() {
        return EJBFraction.createDefaultFraction();
    }

    @Override
    public List<ModelNode> getList(EJBFraction fraction) {
        List<ModelNode> list = new ArrayList<>();

        ModelNode node = new ModelNode();
        node.get(OP_ADDR).set(EXTENSION, "org.jboss.as.ejb3");
        node.get(OP).set(ADD);
        list.add(node);

        try {
            list.addAll(Marshaller.marshal(fraction));
        } catch (Exception e) {
            System.err.println("Cannot configure EJB subsystem. " + e);
        }
        return list;
    }
}
