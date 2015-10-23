package org.wildfly.swarm.jsf.runtime;

import java.util.ArrayList;
import java.util.List;

import org.jboss.dmr.ModelNode;
import org.wildfly.swarm.config.runtime.invocation.Marshaller;
import org.wildfly.swarm.container.runtime.AbstractServerConfiguration;
import org.wildfly.swarm.jsf.JSFFraction;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ADD;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.EXTENSION;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP_ADDR;

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
    public List<ModelNode> getList(JSFFraction fraction) throws Exception {
        List<ModelNode> list = new ArrayList<>();

        ModelNode node = new ModelNode();
        node.get(OP_ADDR).set(EXTENSION, "org.jboss.as.jsf");
        node.get(OP).set(ADD);
        list.add(node);

        list.addAll(Marshaller.marshal(fraction));

        return list;
    }
}
