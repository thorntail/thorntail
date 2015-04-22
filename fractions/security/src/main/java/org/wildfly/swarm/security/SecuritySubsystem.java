package org.wildfly.swarm.security;

import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.PathElement;
import org.jboss.dmr.ModelNode;
import org.wildfly.swarm.container.AbstractSubsystem;
import org.wildfly.swarm.container.Subsystem;

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
public class SecuritySubsystem extends AbstractSubsystem {

    private List<ModelNode> list = new ArrayList<>();

    public SecuritySubsystem() {

        PathAddress address = PathAddress.pathAddress(PathElement.pathElement(SUBSYSTEM, "security"));

        ModelNode node = new ModelNode();
        node.get(OP_ADDR).set(EXTENSION, "org.jboss.as.security");
        node.get(OP).set(ADD);
        this.list.add(node);

        node = new ModelNode();
        node.get( OP_ADDR).set( address.toModelNode() );
        node.get(OP).set(ADD);
        this.list.add( node );

        node = new ModelNode();
        node.get( OP_ADDR).set( address.append("security-domain", "other" ).toModelNode() );
        node.get(OP).set(ADD);
        node.get( "cache-type" ).set( "default" );
        this.list.add( node );

        node = new ModelNode();
        node.get( OP_ADDR).set( address.append("security-domain", "other" ).append( "authentication", "classic" ).toModelNode() );
        node.get(OP).set(ADD);
        this.list.add( node );

        node = new ModelNode();
        node.get( OP_ADDR).set( address.append("security-domain", "other" ).append( "authentication", "classic" ).append( "login-module", "RealmDirect" ).toModelNode() );
        node.get(OP).set(ADD);
        node.get( "code" ).set( "RealmDirect" );
        node.get( "flag" ).set( "required" );
        node.get( "module-options" ).set( "password-stacking", "useFirstPass" );
        this.list.add( node );
    }

    public List<ModelNode> getList() {
        return this.list;
    }

}
