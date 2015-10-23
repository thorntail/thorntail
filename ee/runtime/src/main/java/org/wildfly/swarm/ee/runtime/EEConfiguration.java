package org.wildfly.swarm.ee.runtime;

import java.util.ArrayList;
import java.util.List;

import org.jboss.dmr.ModelNode;
import org.wildfly.swarm.config.runtime.invocation.Marshaller;
import org.wildfly.swarm.container.runtime.AbstractServerConfiguration;
import org.wildfly.swarm.ee.EEFraction;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ADD;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.EXTENSION;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP_ADDR;

/**
 * @author Bob McWhirter
 * @author Lance Ball
 */
public class EEConfiguration extends AbstractServerConfiguration<EEFraction> {

    public EEConfiguration() {
        super(EEFraction.class);
    }

    @Override
    public EEFraction defaultFraction() {
        return EEFraction.createDefaultFraction();
    }

    @Override
    public List<ModelNode> getList(EEFraction fraction) throws Exception {

        List<ModelNode> list = new ArrayList<>();

        ModelNode node = new ModelNode();
        node.get(OP_ADDR).set(EXTENSION, "org.jboss.as.ee");
        node.get(OP).set(ADD);
        list.add(node);

        list.addAll(Marshaller.marshal(fraction));

        return list;

    }
}
