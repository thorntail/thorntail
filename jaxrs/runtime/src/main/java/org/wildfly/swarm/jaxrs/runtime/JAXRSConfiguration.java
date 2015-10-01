package org.wildfly.swarm.jaxrs.runtime;

import org.jboss.dmr.ModelNode;
import org.wildfly.config.invocation.Marshaller;
import org.wildfly.swarm.container.runtime.AbstractServerConfiguration;
import org.wildfly.swarm.jaxrs.JAXRSFraction;

import java.util.ArrayList;
import java.util.List;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.*;

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
    public List<ModelNode> getList(JAXRSFraction fraction) {
        List<ModelNode> list = new ArrayList<>();

        ModelNode node = new ModelNode();
        node.get(OP_ADDR).set(EXTENSION, "org.jboss.as.jaxrs");
        node.get(OP).set(ADD);
        list.add(node);
        try {
            list.addAll(Marshaller.marshal(fraction));
        } catch (Exception e) {
            System.err.println("Cannot configure JAXRS. " + e);
        }

        return list;

    }
}
