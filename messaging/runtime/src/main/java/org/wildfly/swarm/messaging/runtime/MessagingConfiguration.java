package org.wildfly.swarm.messaging.runtime;

import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.PathElement;
import org.jboss.dmr.ModelNode;
import org.jboss.shrinkwrap.api.Archive;
import org.wildfly.swarm.config.runtime.invocation.Marshaller;
import org.wildfly.swarm.container.JARArchive;
import org.wildfly.swarm.container.runtime.AbstractServerConfiguration;
import org.wildfly.swarm.messaging.MessagingFraction;

import java.util.ArrayList;
import java.util.List;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.*;

/**
 * @author Bob McWhirter
 * @author Lance Ball
 */
public class MessagingConfiguration extends AbstractServerConfiguration<MessagingFraction> {

    private PathAddress address = PathAddress.pathAddress(PathElement.pathElement(SUBSYSTEM, "messaging-activemq"));

    public MessagingConfiguration() {
        super(MessagingFraction.class);
    }

    @Override
    public MessagingFraction defaultFraction() {
        return MessagingFraction.createDefaultFraction();
    }

    @Override
    public void prepareArchive(Archive a) {
        a.as(JARArchive.class).addModule("javax.jms.api");
    }

    @Override
    public List<ModelNode> getList(MessagingFraction fraction) {
        List<ModelNode> list = new ArrayList<>();

        ModelNode node = new ModelNode();
        node.get(OP_ADDR).set(EXTENSION, "org.wildfly.extension.messaging-activemq");
        node.get(OP).set(ADD);
        list.add(node);

        try {
            list.addAll(Marshaller.marshal(fraction));
        } catch (Exception e) {
            System.err.println("Cannot configure Logging subsystem. " + e);
        }

        return list;
    }
}
