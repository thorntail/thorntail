package org.wildfly.swarm.runtime.bean.validation;

import java.util.ArrayList;
import java.util.List;

import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.PathElement;
import org.jboss.dmr.ModelNode;
import org.wildfly.swarm.bean.validation.BeanValidationFraction;
import org.wildfly.swarm.runtime.container.AbstractServerConfiguration;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ADD;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.EXTENSION;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP_ADDR;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.SUBSYSTEM;

/**
 * @author Bob McWhirter
 */
public class BeanValidationConfiguration extends AbstractServerConfiguration<BeanValidationFraction> {

    public BeanValidationConfiguration() {
        super(BeanValidationFraction.class);
    }

    @Override
    public BeanValidationFraction defaultFraction() {
        return new BeanValidationFraction();
    }

    @Override
    public List<ModelNode> getList(BeanValidationFraction fraction) {

        List<ModelNode> list = new ArrayList<>();
        PathAddress address = PathAddress.pathAddress(PathElement.pathElement(SUBSYSTEM, "bean-validation"));

        ModelNode node = new ModelNode();
        node.get(OP_ADDR).set(EXTENSION, "org.wildfly.extension.bean-validation");
        node.get(OP).set(ADD);
        list.add(node);

        node = new ModelNode();
        node.get(OP_ADDR).set(address.toModelNode());
        node.get(OP).set(ADD);
        list.add(node);

        return list;

    }
}
