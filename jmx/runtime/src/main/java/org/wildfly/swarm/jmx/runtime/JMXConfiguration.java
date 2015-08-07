package org.wildfly.swarm.jmx.runtime;

import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.PathElement;
import org.jboss.dmr.ModelNode;
import org.wildfly.swarm.container.runtime.AbstractServerConfiguration;
import org.wildfly.swarm.jmx.JMXFraction;

import java.util.ArrayList;
import java.util.List;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.*;

/**
 * @author Bob McWhirter
 */
public class JMXConfiguration extends AbstractServerConfiguration<JMXFraction> {

    public JMXConfiguration() {
        super(JMXFraction.class);
    }

    @Override
    public JMXFraction defaultFraction() {
        return new JMXFraction();
    }

    @Override
    public List<ModelNode> getList(JMXFraction fraction) {
        List<ModelNode> list = new ArrayList<>();

        ModelNode node = new ModelNode();
        node.get(OP_ADDR).set(EXTENSION, "org.jboss.as.jmx");
        node.get(OP).set(ADD);
        list.add(node);

        PathAddress address = PathAddress.pathAddress(PathElement.pathElement(SUBSYSTEM, "jmx"));

        node = new ModelNode();
        node.get(OP_ADDR).set(address.toModelNode());
        node.get(OP).set(ADD);
        list.add(node);

        return list;

    }
}
