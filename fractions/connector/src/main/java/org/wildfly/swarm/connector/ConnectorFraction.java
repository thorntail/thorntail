package org.wildfly.swarm.connector;

import org.jboss.dmr.ModelNode;
import org.wildfly.swarm.container.AbstractFraction;

import java.util.ArrayList;
import java.util.List;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ADD;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.EXTENSION;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP_ADDR;

/**
 * @author Bob McWhirter
 */
public class ConnectorFraction extends AbstractFraction {

    private List<ModelNode> list = new ArrayList<>();

    public ConnectorFraction() {
        super( -10 );
        ModelNode node = new ModelNode();
        node.get(OP_ADDR).set(EXTENSION, "org.jboss.as.connector");
        node.get(OP).set(ADD);
        this.list.add(node);
    }

    @Override
    public List<ModelNode> getList() {
        return this.list;
    }
}
