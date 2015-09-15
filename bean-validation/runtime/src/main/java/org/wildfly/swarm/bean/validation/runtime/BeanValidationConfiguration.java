package org.wildfly.swarm.bean.validation.runtime;

import java.util.ArrayList;
import java.util.List;

import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.PathElement;
import org.jboss.dmr.ModelNode;
import org.wildfly.apigen.invocation.Marshaller;
import org.wildfly.swarm.bean.validation.BeanValidationFraction;
import org.wildfly.swarm.config.bean.validation.BeanValidation;
import org.wildfly.swarm.container.runtime.AbstractServerConfiguration;

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

        BeanValidation beanValidation = new BeanValidation();
        try {
            list.addAll(Marshaller.marshal(beanValidation));
        } catch (Exception e) {
            System.err.println("Unable to configure bean-validation " + e);
        }

        return list;

    }
}
