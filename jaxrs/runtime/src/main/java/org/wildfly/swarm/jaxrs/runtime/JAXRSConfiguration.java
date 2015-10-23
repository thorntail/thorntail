package org.wildfly.swarm.jaxrs.runtime;

import java.util.ArrayList;
import java.util.List;

import org.jboss.dmr.ModelNode;
import org.wildfly.swarm.config.runtime.invocation.Marshaller;
import org.wildfly.swarm.container.runtime.AbstractServerConfiguration;
import org.wildfly.swarm.jaxrs.JAXRSFraction;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ADD;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.EXTENSION;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP_ADDR;

/**
 * @author Bob McWhirter
 */
public class JAXRSConfiguration extends AbstractServerConfiguration<JAXRSFraction> {

    public JAXRSConfiguration() {
        super(JAXRSFraction.class);
    }

    @Override
    public JAXRSFraction defaultFraction() {
        return new JAXRSFraction();
    }

    @Override
    public List<ModelNode> getList(JAXRSFraction fraction) throws Exception {
        List<ModelNode> list = new ArrayList<>();

        ModelNode node = new ModelNode();
        node.get(OP_ADDR).set(EXTENSION, "org.jboss.as.jaxrs");
        node.get(OP).set(ADD);
        list.add(node);

        list.addAll(Marshaller.marshal(fraction));

        return list;

    }
}
