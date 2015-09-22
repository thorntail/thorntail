package org.wildfly.swarm.io.runtime;

import java.util.ArrayList;
import java.util.List;

import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.PathElement;
import org.jboss.dmr.ModelNode;
import org.wildfly.apigen.invocation.Marshaller;
import org.wildfly.swarm.config.io.Io;
import org.wildfly.swarm.config.io.subsystem.bufferPool.BufferPool;
import org.wildfly.swarm.config.io.subsystem.worker.Worker;
import org.wildfly.swarm.container.runtime.AbstractServerConfiguration;
import org.wildfly.swarm.io.IOFraction;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ADD;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.EXTENSION;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP_ADDR;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.SUBSYSTEM;

/**
 * @author Bob McWhirter
 */
public class IOConfiguration extends AbstractServerConfiguration<IOFraction> {

    public IOConfiguration() {
        super(IOFraction.class);
    }

    @Override
    public IOFraction defaultFraction() {
        return IOFraction.createDefaultFraction();
    }

    @Override
    public List<ModelNode> getList(IOFraction fraction) {
        List<ModelNode> list = new ArrayList<>();

        ModelNode node = new ModelNode();
        node.get(OP_ADDR).set(EXTENSION, "org.wildfly.extension.io");
        node.get(OP).set(ADD);
        list.add(node);

        try {
            list.addAll(Marshaller.marshal(fraction));
        } catch (Exception e) {
            System.err.println("Cannot configure IO subsystem " + e);
        }

        return list;

    }
}
