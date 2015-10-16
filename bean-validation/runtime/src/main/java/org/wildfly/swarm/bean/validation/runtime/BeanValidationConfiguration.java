package org.wildfly.swarm.bean.validation.runtime;

import org.jboss.dmr.ModelNode;
import org.wildfly.swarm.bean.validation.BeanValidationFraction;
import org.wildfly.swarm.config.runtime.invocation.Marshaller;
import org.wildfly.swarm.container.runtime.AbstractServerConfiguration;

import java.util.ArrayList;
import java.util.List;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.*;

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

        ModelNode node = new ModelNode();
        node.get(OP_ADDR).set(EXTENSION, "org.wildfly.extension.bean-validation");
        node.get(OP).set(ADD);
        list.add(node);

        try {
            list.addAll(Marshaller.marshal(fraction));
        } catch (Exception e) {
            System.err.println("Unable to configure bean-validation " + e);
        }

        return list;

    }
}
