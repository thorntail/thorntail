package org.wildfly.swarm.undertow;

import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.PathElement;
import org.jboss.dmr.ModelNode;
import org.wildfly.swarm.container.AbstractFraction;

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
public class UndertowFraction extends AbstractFraction {

    private List<ModelNode> list = new ArrayList<>();

    public UndertowFraction() {

        PathAddress address = PathAddress.pathAddress(PathElement.pathElement(SUBSYSTEM, "undertow"));

        ModelNode node = new ModelNode();
        node.get(OP_ADDR).set(EXTENSION, "org.wildfly.extension.undertow");
        node.get(OP).set(ADD);
        this.list.add(node);

        node = new ModelNode();
        node.get(OP_ADDR).set(address.toModelNode());
        node.get(OP).set(ADD);
        this.list.add(node);

        node = new ModelNode();
        node.get(OP_ADDR).set(address.append("server", "default-server").toModelNode());
        node.get(OP).set(ADD);
        this.list.add(node);

        node = new ModelNode();
        node.get(OP_ADDR).set(address.append("buffer-cache", "default").toModelNode());
        node.get(OP).set(ADD);
        this.list.add(node);

        node = new ModelNode();
        node.get(OP_ADDR).set(address.append("configuration", "handler").toModelNode());
        node.get(OP).set(ADD);
        this.list.add(node);


        node = new ModelNode();
        node.get(OP_ADDR).set(address.append("server", "default-server").append("http-listener", "default").toModelNode());
        node.get(OP).set(ADD);
        node.get(SOCKET_BINDING).set("http");
        this.list.add(node);

        node = new ModelNode();
        node.get(OP_ADDR).set(address.append("server", "default-server").append("host", "default-host").toModelNode());
        node.get(OP).set(ADD);
        node.get("alias").setEmptyList().add( "localhost" );
        this.list.add(node);

        node = new ModelNode();
        node.get(OP_ADDR).set(address.append( "servlet-container", "default" ).toModelNode() );
        node.get(OP).set(ADD);
        this.list.add( node );
    }

    public List<ModelNode> getList() {
        return this.list;
    }


}
