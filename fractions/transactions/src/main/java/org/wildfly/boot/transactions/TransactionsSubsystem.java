package org.wildfly.boot.transactions;

import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.PathElement;
import org.jboss.dmr.ModelNode;
import org.wildfly.boot.container.Subsystem;

import java.util.ArrayList;
import java.util.List;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ADD;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.EXTENSION;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP_ADDR;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.PORT;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.SUBSYSTEM;

/**
 * @author Bob McWhirter
 */
public class TransactionsSubsystem implements Subsystem {

    private List<ModelNode> list = new ArrayList<>();

    public TransactionsSubsystem(int port, int statusPort) {

        PathAddress address = PathAddress.pathAddress(PathElement.pathElement(SUBSYSTEM, "transactions"));

        ModelNode node = new ModelNode();
        node.get(OP_ADDR).set(EXTENSION, "org.jboss.as.transactions");
        node.get(OP).set(ADD);
        this.list.add(node);

        node = new ModelNode();
        node.get(OP_ADDR).set(address.toModelNode());
        node.get(OP).set(ADD);
        node.get("socket-binding").set( "txn-recovery-environment");
        node.get("status-socket-binding").set( "txn-status-manager");
        node.get("process-id-uuid").set( true );
        this.list.add(node);

        node = new ModelNode();
        node.get(OP_ADDR).set( PathAddress.pathAddress( PathElement.pathElement("socket-binding-group", "default-sockets" ) ).append( "socket-binding", "txn-recovery-environment" ).toModelNode() );
        node.get( OP ).set( ADD );
        node.get( PORT ).set( port );
        this.list.add( node );

        node = new ModelNode();
        node.get(OP_ADDR).set( PathAddress.pathAddress( PathElement.pathElement("socket-binding-group", "default-sockets" ) ).append( "socket-binding", "txn-status-manager" ).toModelNode() );
        node.get( OP ).set( ADD );
        node.get( PORT ).set( statusPort );
        this.list.add( node );
    }

    public List<ModelNode> getList() {
        return this.list;
    }

}
