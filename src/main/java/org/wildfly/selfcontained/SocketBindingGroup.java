package org.wildfly.selfcontained;

import org.jboss.as.controller.PathAddress;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.ValueExpression;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ADD;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.DEFAULT_INTERFACE;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.INET_ADDRESS;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP_ADDR;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.PORT;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.PORT_OFFSET;

/**
 * @author Bob McWhirter
 */
public class SocketBindingGroup {

    private final Container container;
    private final PathAddress address;

    public SocketBindingGroup(Container container, PathAddress address) {
        this.container = container;
        this.address = address;
    }

    public SocketBindingGroup socketBinding(String name, String portExpression) {

        ModelNode node = new ModelNode();

        node.get( OP_ADDR ).set( this.address.append( "socket-binding", name ).toModelNode() );
        node.get( OP ).set( ADD );
        node.get( PORT ).set( new ValueExpression( portExpression ) );

        this.container.list.add( node );

        return this;
    }

    public SocketBindingGroup socketBinding(String name, int port) {
        ModelNode node = new ModelNode();

        node.get( OP_ADDR ).set( this.address.append( "socket-binding", name ).toModelNode() );
        node.get( OP ).set( ADD );
        node.get( PORT ).set( port );

        this.container.list.add( node );

        return this;
    }

    public Container end() {
        return this.container;
    }
}
