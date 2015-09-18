package org.wildfly.swarm.remoting.runtime;

import java.util.ArrayList;
import java.util.List;

import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.PathElement;
import org.jboss.dmr.ModelNode;
import org.wildfly.apigen.invocation.Marshaller;
import org.wildfly.swarm.config.remoting.subsystem.configuration.Endpoint;
import org.wildfly.swarm.config.remoting.subsystem.httpConnector.HttpConnector;
import org.wildfly.swarm.container.runtime.AbstractServerConfiguration;
import org.wildfly.swarm.remoting.RemotingFraction;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ADD;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.EXTENSION;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP_ADDR;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.SUBSYSTEM;

/**
 * @author Ken Finnigan
 * @author Lance Ball
 */
public class RemotingConfiguration extends AbstractServerConfiguration<RemotingFraction> {

    public RemotingConfiguration() {
        super(RemotingFraction.class);
    }

    @Override
    public RemotingFraction defaultFraction() {

        RemotingFraction fraction = new RemotingFraction();
        fraction.endpoint(new Endpoint())
                .httpConnector(new HttpConnector("http-remoting-connector")
                        .connectorRef("default"));
        return fraction;
    }

    @Override
    public List<ModelNode> getList(RemotingFraction fraction) {
        List<ModelNode> list = new ArrayList<>();

        ModelNode node = new ModelNode();
        node.get(OP_ADDR).set(EXTENSION, "org.jboss.as.remoting");
        node.get(OP).set(ADD);
        list.add(node);

        try {
            list.addAll(Marshaller.marshal(fraction));
        } catch (Exception e) {
            System.err.println("Cannot configure Remoting subsystem. " + e);
        }

        return list;
    }
}
