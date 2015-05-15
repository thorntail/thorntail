package org.wildfly.swarm.datasources;

import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.PathElement;
import org.jboss.dmr.ModelNode;
import org.wildfly.swarm.container.AbstractFraction;

import java.util.ArrayList;
import java.util.List;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ADD;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP_ADDR;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.SUBSYSTEM;

/**
 * @author Bob McWhirter
 */
public class DatasourcesFraction extends AbstractFraction {

    private List<ModelNode> list = new ArrayList<>();
    private PathAddress address = PathAddress.pathAddress(PathElement.pathElement(SUBSYSTEM, "datasources"));

    public DatasourcesFraction() {
        ModelNode node = new ModelNode();
        node.get(OP_ADDR).set(address.toModelNode());
        node.get(OP).set(ADD);
        this.list.add(node);
    }

    public DatasourcesFraction datasource(Datasource datasource) {
        this.list.add(datasource.get(address));
        return this;
    }

    public DatasourcesFraction driver(Driver driver) {
        this.list.add(driver.get(address));
        return this;
    }

    @Override
    public List<ModelNode> getList() {
        return this.list;
    }
}
