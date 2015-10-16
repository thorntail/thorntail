package org.wildfly.swarm.jsf.runtime;

import org.jboss.dmr.ModelNode;
import org.wildfly.swarm.config.runtime.invocation.Marshaller;
import org.wildfly.swarm.container.runtime.AbstractServerConfiguration;
import org.wildfly.swarm.jsf.JSFFraction;

import java.util.ArrayList;
import java.util.List;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.*;

/**
 * @author Ken Finnigan
 * @author Lance Ball
 */
public class JSFConfiguration extends AbstractServerConfiguration<JSFFraction> {

    public JSFConfiguration() {
        super(JSFFraction.class);
    }

    @Override
    public JSFFraction defaultFraction() {
        return new JSFFraction();
    }

    @Override
    public List<ModelNode> getList(JSFFraction fraction) {
        List<ModelNode> list = new ArrayList<>();

        ModelNode node = new ModelNode();
        node.get(OP_ADDR).set(EXTENSION, "org.jboss.as.jsf");
        node.get(OP).set(ADD);
        list.add(node);

        try {
            list.addAll(Marshaller.marshal(fraction));
        } catch (Exception e) {
            System.err.println("Cannot configure JSF subsystem. " + e);
        }

        return list;
    }
}
