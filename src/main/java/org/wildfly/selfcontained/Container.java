package org.wildfly.selfcontained;

import org.jboss.as.controller.PathAddress;
import org.jboss.as.server.SelfContainedContainer;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.ValueExpression;

import java.util.ArrayList;
import java.util.List;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ADD;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.DEFAULT_INTERFACE;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.INET_ADDRESS;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP_ADDR;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.PORT_OFFSET;

/**
 * @author Bob McWhirter
 */
public class Container {

    List<ModelNode> list = new ArrayList<>();
    private List<Subsystem> subsystems = new ArrayList<>();

    private SelfContainedContainer container;

    public Container() {

    }

    public Container subsystem(Subsystem subsystem) {
        this.subsystems.add( subsystem );
        return this;
    }

    public Container iface(String name, String expression) {
        ModelNode node = new ModelNode();

        node.get(OP).set(ADD);
        node.get(OP_ADDR).set("interface", name);
        node.get(INET_ADDRESS).set(new ValueExpression(expression));

        list.add( node );

        return this;
    }

    public SocketBindingGroup socketBindingGroup(String name, String iface, String portOffsetExpression) {
        ModelNode node = new ModelNode();

        PathAddress address = PathAddress.pathAddress( "socket-binding-group", name );
        node.get(OP).set(ADD);
        node.get(OP_ADDR).set( address.toModelNode() );
        node.get(DEFAULT_INTERFACE).set( iface );
        node.get(PORT_OFFSET).set( new ValueExpression(portOffsetExpression));

        list.add( node );

        return new SocketBindingGroup( this, address );
    }

    public void start() {
        this.container = new SelfContainedContainer();
        this.container.start( getList() );
    }

    List<ModelNode> getList() {
        List<ModelNode> fullList = new ArrayList<>();

        fullList.addAll( this.list );

        for ( Subsystem each : this.subsystems ) {
            fullList.addAll( each.getList() );
        }

        return fullList;
    }
}
