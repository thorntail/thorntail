package org.wildfly.swarm.weld.runtime;

import org.jboss.dmr.ModelNode;
import org.wildfly.swarm.config.runtime.invocation.Marshaller;
import org.wildfly.swarm.container.runtime.AbstractServerConfiguration;
import org.wildfly.swarm.weld.WeldFraction;

import java.util.ArrayList;
import java.util.List;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.*;

/**
 * @author Bob McWhirter
 * @author Lance Ball
 */
public class WeldConfiguration extends AbstractServerConfiguration<WeldFraction> {

    public WeldConfiguration() {
        super(WeldFraction.class);
    }

    @Override
    public WeldFraction defaultFraction() {
        return new WeldFraction();
    }

    @Override
    public List<ModelNode> getList(WeldFraction fraction) {
        List<ModelNode> list = new ArrayList<>();

        ModelNode node = new ModelNode();
        node.get(OP_ADDR).set(EXTENSION, "org.jboss.as.weld");
        node.get(OP).set(ADD);
        list.add(node);
        try {
            list.addAll(Marshaller.marshal(fraction));
        } catch (Exception e) {
            System.err.println("Cannot configure Weld subysystem. "+ e);
        }

        return list;

    }
}
