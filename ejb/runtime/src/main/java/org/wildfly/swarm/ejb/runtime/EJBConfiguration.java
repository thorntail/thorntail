package org.wildfly.swarm.ejb.runtime;

import java.util.ArrayList;
import java.util.List;

import org.jboss.dmr.ModelNode;
import org.wildfly.swarm.config.runtime.invocation.Marshaller;
import org.wildfly.swarm.container.runtime.AbstractServerConfiguration;
import org.wildfly.swarm.ejb.EJBFraction;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ADD;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.EXTENSION;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP_ADDR;

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
    public List<ModelNode> getList(EJBFraction fraction) throws Exception {
        List<ModelNode> list = new ArrayList<>();

        ModelNode node = new ModelNode();
        node.get(OP_ADDR).set(EXTENSION, "org.jboss.as.ejb3");
        node.get(OP).set(ADD);
        list.add(node);

        list.addAll(Marshaller.marshal(fraction));

        return list;
    }
}
