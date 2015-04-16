package org.wildfly.swarm.container;

import org.jboss.as.controller.PathAddress;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.ValueExpression;

import java.util.ArrayList;
import java.util.List;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ADD;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP_ADDR;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.PORT;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.DEFAULT_INTERFACE;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.PORT_OFFSET;


/**
 * @author Bob McWhirter
 */
public class SocketBindingGroup {

    private final List<ModelNode> list = new ArrayList<>();
    private final PathAddress address;

    public SocketBindingGroup(String name, String defaultInterface, String portOffsetExpression) {
        ModelNode node = new ModelNode();

        this.address = PathAddress.pathAddress("socket-binding-group", name);
        node.get(OP).set(ADD);
        node.get(OP_ADDR).set(address.toModelNode());
        node.get(DEFAULT_INTERFACE).set(defaultInterface);
        node.get(PORT_OFFSET).set(new ValueExpression(portOffsetExpression));

        list.add(node);
    }

    public SocketBindingGroup socketBinding(String name, String portExpression) {

        ModelNode node = new ModelNode();

        node.get( OP_ADDR ).set( this.address.append( "socket-binding", name ).toModelNode() );
        node.get( OP ).set( ADD );
        node.get( PORT ).set( new ValueExpression( portExpression ) );

        this.list.add( node );

        return this;
    }

    public SocketBindingGroup socketBinding(String name, int port) {
        ModelNode node = new ModelNode();

        node.get( OP_ADDR ).set( this.address.append( "socket-binding", name ).toModelNode() );
        node.get( OP ).set( ADD );
        node.get( PORT ).set( port );

        this.list.add( node );

        return this;
    }

    public List<ModelNode> getList() {
        return this.list;
    }
}
