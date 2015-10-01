package org.wildfly.swarm.ee.runtime;

import org.jboss.dmr.ModelNode;
import org.wildfly.config.invocation.Marshaller;
import org.wildfly.swarm.container.runtime.AbstractServerConfiguration;
import org.wildfly.swarm.ee.EEFraction;

import java.util.ArrayList;
import java.util.List;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.*;

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
    public List<ModelNode> getList(EEFraction fraction) {

        List<ModelNode> list = new ArrayList<>();

        ModelNode node = new ModelNode();
        node.get(OP_ADDR).set(EXTENSION, "org.jboss.as.ee");
        node.get(OP).set(ADD);
        list.add(node);

        try {
            list.addAll(Marshaller.marshal(fraction));
        } catch (Exception e) {
            System.err.println("Cannot configure EE subsystem " + e);
        }
        return list;

    }
}
