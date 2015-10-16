package org.wildfly.swarm.remoting.runtime;

import org.jboss.dmr.ModelNode;
import org.wildfly.swarm.config.remoting.EndpointConfiguration;
import org.wildfly.swarm.config.remoting.HttpConnector;
import org.wildfly.swarm.config.runtime.invocation.Marshaller;
import org.wildfly.swarm.container.runtime.AbstractServerConfiguration;
import org.wildfly.swarm.remoting.RemotingFraction;

import java.util.ArrayList;
import java.util.List;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.*;

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
        fraction.endpointConfiguration(new EndpointConfiguration())
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
