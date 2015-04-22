package org.wildfly.swarm.datasources;

import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.PathElement;
import org.jboss.dmr.ModelNode;
import org.wildfly.swarm.container.AbstractSubsystem;
import org.wildfly.swarm.container.Subsystem;

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
public class DatasourcesSubsystem extends AbstractSubsystem {

    private List<ModelNode> list = new ArrayList<>();
    private PathAddress address = PathAddress.pathAddress(PathElement.pathElement(SUBSYSTEM, "datasources"));

    public DatasourcesSubsystem() {
        ModelNode node = new ModelNode();
        node.get(OP_ADDR).set(EXTENSION, "org.jboss.as.connector");
        node.get(OP).set(ADD);
        this.list.add(node);

        node = new ModelNode();
        node.get(OP_ADDR).set(address.toModelNode());
        node.get(OP).set(ADD);
        this.list.add(node);
    }

    public DatasourcesSubsystem datasource(Datasource datasource) {
        this.list.add(datasource.get(address));
        return this;
    }

    public DatasourcesSubsystem driver(Driver driver) {
        this.list.add(driver.get(address));
        return this;
    }

    @Override
    public List<ModelNode> getList() {
        return this.list;
    }
}
