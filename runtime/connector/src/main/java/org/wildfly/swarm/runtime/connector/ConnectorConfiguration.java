package org.wildfly.swarm.runtime.connector;

import java.util.ArrayList;
import java.util.List;

import org.jboss.dmr.ModelNode;
import org.wildfly.swarm.connector.ConnectorFraction;
import org.wildfly.swarm.runtime.container.AbstractServerConfiguration;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ADD;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.EXTENSION;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP_ADDR;

/**
 * @author Bob McWhirter
 */
public class ConnectorConfiguration extends AbstractServerConfiguration<ConnectorFraction> {

    public ConnectorConfiguration() {
        super(ConnectorFraction.class);
    }

    @Override
    public ConnectorFraction defaultFraction() {
        return new ConnectorFraction();
    }

    @Override
    public List<ModelNode> getList(ConnectorFraction fraction) {
        List<ModelNode> list = new ArrayList<>();

        ModelNode node = new ModelNode();
        node.get(OP_ADDR).set(EXTENSION, "org.jboss.as.connector");
        node.get(OP).set(ADD);
        list.add(node);

        return list;

    }
}
