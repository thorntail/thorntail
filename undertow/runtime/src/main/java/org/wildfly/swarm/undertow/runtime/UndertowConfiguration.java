package org.wildfly.swarm.undertow.runtime;

import java.util.ArrayList;
import java.util.List;

import io.undertow.Undertow;
import org.jboss.dmr.ModelNode;
import org.wildfly.swarm.config.runtime.invocation.Marshaller;
import org.wildfly.swarm.container.runtime.AbstractServerConfiguration;
import org.wildfly.swarm.undertow.UndertowFraction;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ADD;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.EXTENSION;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP_ADDR;

/**
 * @author Bob McWhirter
 * @author Lance Ball
 */
public class UndertowConfiguration extends AbstractServerConfiguration<UndertowFraction> {

    public UndertowConfiguration() {
        super(UndertowFraction.class);
    }

    @Override
    public UndertowFraction defaultFraction() {
        return UndertowFraction.createDefaultFraction();
    }

    @Override
    public List<ModelNode> getList(UndertowFraction fraction) {
        List<ModelNode> list = new ArrayList<>();

        ModelNode node = new ModelNode();
        node.get(OP_ADDR).set(EXTENSION, "org.wildfly.extension.undertow");
        node.get(OP).set(ADD);
        list.add(node);

        try {
            list.addAll(Marshaller.marshal(fraction));
        } catch (Exception e) {
            System.err.println("Unable to configure Undertow subsystem. " + e);
        }

        return list;

    }
}
