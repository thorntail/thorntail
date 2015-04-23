package org.wildfly.swarm.connector;

import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.PathElement;
import org.jboss.dmr.ModelNode;
import org.wildfly.swarm.container.AbstractSubsystem;

import java.util.ArrayList;
import java.util.List;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ADD;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.EXTENSION;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP_ADDR;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.SUBSYSTEM;

/**
 * @author Bob McWhirter
 */
public class ConnectorSubsystem extends AbstractSubsystem {

    private List<ModelNode> list = new ArrayList<>();

    public ConnectorSubsystem() {
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
