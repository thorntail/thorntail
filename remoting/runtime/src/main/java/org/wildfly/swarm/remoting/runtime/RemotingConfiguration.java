package org.wildfly.swarm.remoting.runtime;

import java.util.ArrayList;
import java.util.List;

import org.jboss.dmr.ModelNode;
import org.wildfly.swarm.config.remoting.EndpointConfiguration;
import org.wildfly.swarm.config.remoting.HTTPConnector;
import org.wildfly.swarm.config.runtime.invocation.Marshaller;
import org.wildfly.swarm.container.runtime.AbstractServerConfiguration;
import org.wildfly.swarm.remoting.RemotingFraction;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ADD;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.EXTENSION;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP_ADDR;

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
                .httpConnector(new HTTPConnector("http-remoting-connector")
                        .connectorRef("default"));
        return fraction;
    }

    @Override
    public List<ModelNode> getList(RemotingFraction fraction) throws Exception {
        List<ModelNode> list = new ArrayList<>();

        ModelNode node = new ModelNode();
        node.get(OP_ADDR).set(EXTENSION, "org.jboss.as.remoting");
        node.get(OP).set(ADD);
        list.add(node);

        list.addAll(Marshaller.marshal(fraction));

        return list;
    }
}
