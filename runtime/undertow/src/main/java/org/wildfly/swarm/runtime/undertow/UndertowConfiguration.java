package org.wildfly.swarm.runtime.undertow;

import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.PathElement;
import org.jboss.dmr.ModelNode;
import org.wildfly.swarm.logging.ConsoleHandler;
import org.wildfly.swarm.logging.Formatter;
import org.wildfly.swarm.logging.LoggingFraction;
import org.wildfly.swarm.logging.RootLogger;
import org.wildfly.swarm.runtime.container.AbstractServerConfiguration;
import org.wildfly.swarm.undertow.UndertowFraction;

import java.util.ArrayList;
import java.util.List;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ADD;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.EXTENSION;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP_ADDR;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.SOCKET_BINDING;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.SUBSYSTEM;

/**
 * @author Bob McWhirter
 */
public class UndertowConfiguration extends AbstractServerConfiguration<UndertowFraction> {

    public UndertowConfiguration() {
        super(UndertowFraction.class);
    }

    @Override
    public UndertowFraction defaultFraction() {
        return new UndertowFraction();
    }

    @Override
    public List<ModelNode> getList(UndertowFraction fraction) {
        List<ModelNode> list = new ArrayList<>();

        PathAddress address = PathAddress.pathAddress(PathElement.pathElement(SUBSYSTEM, "undertow"));

        ModelNode node = new ModelNode();
        node.get(OP_ADDR).set(EXTENSION, "org.wildfly.extension.undertow");
        node.get(OP).set(ADD);
        list.add(node);

        node = new ModelNode();
        node.get(OP_ADDR).set(address.toModelNode());
        node.get(OP).set(ADD);
        list.add(node);

        node = new ModelNode();
        node.get(OP_ADDR).set(address.append("server", "default-server").toModelNode());
        node.get(OP).set(ADD);
        list.add(node);

        node = new ModelNode();
        node.get(OP_ADDR).set(address.append("buffer-cache", "default").toModelNode());
        node.get(OP).set(ADD);
        list.add(node);

        node = new ModelNode();
        node.get(OP_ADDR).set(address.append("configuration", "handler").toModelNode());
        node.get(OP).set(ADD);
        list.add(node);


        node = new ModelNode();
        node.get(OP_ADDR).set(address.append("server", "default-server").append("http-listener", "default").toModelNode());
        node.get(OP).set(ADD);
        node.get(SOCKET_BINDING).set("http");
        list.add(node);

        node = new ModelNode();
        node.get(OP_ADDR).set(address.append("server", "default-server").append("host", "default-host").toModelNode());
        node.get(OP).set(ADD);
        node.get("alias").setEmptyList().add( "localhost" );
        list.add(node);

        node = new ModelNode();
        node.get(OP_ADDR).set(address.append( "servlet-container", "default" ).toModelNode() );
        node.get(OP).set(ADD);
        list.add( node );


        return list;

    }
}
