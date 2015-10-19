package org.wildfly.swarm.infinispan.runtime;

import java.util.ArrayList;
import java.util.List;

import org.jboss.dmr.ModelNode;
import org.wildfly.swarm.config.runtime.invocation.Marshaller;
import org.wildfly.swarm.container.runtime.AbstractServerConfiguration;
import org.wildfly.swarm.infinispan.InfinispanFraction;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ADD;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.EXTENSION;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP_ADDR;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.SUBSYSTEM;

/**
 * @author Lance Ball
 */
public class InfinispanConfiguration extends AbstractServerConfiguration<InfinispanFraction> {

    public InfinispanConfiguration() {
        super(InfinispanFraction.class);
    }

    @Override
    public InfinispanFraction defaultFraction() {
        return InfinispanFraction.createDefaultFraction();
    }

    @Override
    public List<ModelNode> getList(InfinispanFraction fraction) {
        List<ModelNode> list = new ArrayList<>();

        ModelNode node = new ModelNode();
        node.get(OP_ADDR).set(EXTENSION, "org.jboss.as.clustering.infinispan");
        node.get(OP).set(ADD);
        list.add(node);
        try {
            list.addAll(Marshaller.marshal(fraction));
        } catch (Exception e) {
            System.err.println("Cannot configure Infinispan subsystem. " + e);
        }

        return list;

    }
}
