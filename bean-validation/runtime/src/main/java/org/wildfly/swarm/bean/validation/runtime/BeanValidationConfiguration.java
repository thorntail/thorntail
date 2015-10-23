package org.wildfly.swarm.bean.validation.runtime;

import java.util.ArrayList;
import java.util.List;

import org.jboss.dmr.ModelNode;
import org.wildfly.swarm.bean.validation.BeanValidationFraction;
import org.wildfly.swarm.config.runtime.invocation.Marshaller;
import org.wildfly.swarm.container.runtime.AbstractServerConfiguration;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ADD;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.EXTENSION;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP_ADDR;

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
    public List<ModelNode> getList(BeanValidationFraction fraction) throws Exception {

        List<ModelNode> list = new ArrayList<>();

        ModelNode node = new ModelNode();
        node.get(OP_ADDR).set(EXTENSION, "org.wildfly.extension.bean-validation");
        node.get(OP).set(ADD);
        list.add(node);

        list.addAll(Marshaller.marshal(fraction));

        return list;
    }
}
