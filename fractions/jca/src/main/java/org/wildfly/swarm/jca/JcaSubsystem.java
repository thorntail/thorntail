package org.wildfly.swarm.jca;

import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.PathElement;
import org.jboss.dmr.ModelNode;
import org.wildfly.swarm.container.AbstractSubsystem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ADD;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.EXTENSION;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP_ADDR;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.SUBSYSTEM;

/**
 * @author Bob McWhirter
 */
public class JcaSubsystem extends AbstractSubsystem {

    private List<ModelNode> list = new ArrayList<>();

    private PathAddress address = PathAddress.pathAddress(PathElement.pathElement(SUBSYSTEM, "jca"));

    public JcaSubsystem() {
        ModelNode node = new ModelNode();
        node.get(OP_ADDR).set(address.toModelNode());
        node.get(OP).set(ADD);
        this.list.add(node);

        node = new ModelNode();
        node.get(OP_ADDR).set(address.append("archive-validation", "archive-validation").toModelNode());
        node.get(OP).set(ADD);
        node.get("enable").set(true);
        node.get("fail-on-error").set(true);
        node.get("fail-on-warn").set(true);
        list.add(node);

        node = new ModelNode();
        node.get(OP_ADDR).set(address.append("bean-validation", "bean-validation").toModelNode());
        node.get(OP).set(ADD);
        node.get("enable").set(true);
        list.add(node);

        node = new ModelNode();
        node.get(OP_ADDR).set(address.append("workmanager", "default").toModelNode());
        node.get(OP).set(ADD);
        node.get( "name" ).set( "default" );
        list.add(node);

        node = new ModelNode();
        node.get(OP_ADDR).set(address.append("workmanager", "default").append("short-running-threads", "default").toModelNode());
        node.get(OP).set(ADD);
        node.get("core-threads").set(50);
        node.get("queue-length").set(50);
        node.get("max-threads").set(50);
        node.get("keepalive-timeout").set("time", 10L);
        node.get("keepalive-timeout").set("unit", "SECONDS");
        list.add(node);

        node = new ModelNode();
        node.get(OP_ADDR).set(address.append("workmanager", "default").append("long-running-threads", "default").toModelNode());
        node.get(OP).set(ADD);
        node.get("core-threads").set(50);
        node.get("queue-length").set(50);
        node.get("max-threads").set(50);
        node.get("keepalive-timeout").set("time", 10L);
        node.get("keepalive-timeout").set("unit", "SECONDS");
        list.add(node);

        node = new ModelNode();
        node.get( OP_ADDR).set( address.append( "bootstrap-context", "default" ).toModelNode() );
        node.get( OP ).set( ADD );
        node.get( "workmanager" ).set( "default" );
        node.get( "name" ).set( "default" );
        list.add(node);

        node = new ModelNode();
        node.get( OP_ADDR ).set( address.append( "cached-connection-manager", "cached-connection-manager" ).toModelNode() );
        node.get( OP ).set( ADD );
        node.get( "install" ).set( true );
        list.add( node );
    }

    @Override
    public List<ModelNode> getList() {
        return this.list;
    }
}
