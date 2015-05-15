package org.wildfly.swarm.ee;

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
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.SUBSYSTEM;

/**
 * @author Bob McWhirter
 */
public class EeFraction extends AbstractFraction {

    private List<ModelNode> list = new ArrayList<>();

    public EeFraction() {

        PathAddress address = PathAddress.pathAddress(PathElement.pathElement(SUBSYSTEM, "ee"));

        ModelNode node = new ModelNode();
        node.get(OP_ADDR).set(EXTENSION, "org.jboss.as.ee");
        node.get(OP).set(ADD);
        this.list.add(node);

        node = new ModelNode();
        node.get(OP_ADDR).set(address.toModelNode());
        node.get(OP).set(ADD);
        node.get("spec-descriptor-property-replacement").set(false);
        this.list.add(node);

        node = new ModelNode();
        node.get(OP_ADDR).set(address.append("context-service", "default").toModelNode());
        node.get(OP).set(ADD);
        node.get( "jndi-name" ).set( "java:jboss/ee/concurrency/context/default" );
        node.get( "use-transaction-setup-provider" ).set( false );
        this.list.add(node);

        node = new ModelNode();
        node.get(OP_ADDR).set(address.append("managed-thread-factory", "default").toModelNode());
        node.get(OP).set(ADD);
        node.get( "jndi-name" ).set( "java:jboss/ee/concurrency/factory/default" );
        node.get( "content-service" ).set( "default" );
        this.list.add(node);

        node = new ModelNode();
        node.get(OP_ADDR).set(address.append("managed-executor-service", "default").toModelNode());
        node.get(OP).set(ADD);
        node.get( "jndi-name" ).set( "java:jboss/ee/concurrency/executor/default" );
        node.get("context-service").set("default");
        node.get("hung-task-threshold").set(60000L);
        node.get("core-threads").set(5);
        node.get("max-threads").set(25);
        node.get("keepalive-time").set(5000L);
        this.list.add(node);

        node = new ModelNode();
        node.get(OP_ADDR).set(address.append("managed-scheduled-executor-service", "default").toModelNode());
        node.get(OP).set(ADD);
        node.get( "jndi-name" ).set( "java:jboss/ee/concurrency/scheduler/default" );
        node.get("context-service").set("default");
        node.get("hung-task-threshold").set(60000L);
        node.get("core-threads").set(5);
        node.get("keepalive-time").set(3000L);
        this.list.add(node);

        /*
        node = new ModelNode();
        node.get(OP_ADDR).set(address.append("service", "default-bindings").toModelNode());
        node.get(OP).set(ADD);
        node.get( "context-service" ).set( "java:jboss/ee/concurrency/context/default" );
        node.get( "managed-executor-service" ).set( "java:jboss/ee/concurrency/executor/default" );
        node.get( "managed-scheduled-executor-service" ).set( "java:jboss/ee/concurrency/scheduler/default");
        node.get( "managed-thread-factory").set( "java:jboss/ee/concurrency/factory/default" );
        this.list.add( node );
        */

    }

    public List<ModelNode> getList() {
        return this.list;
    }

}
