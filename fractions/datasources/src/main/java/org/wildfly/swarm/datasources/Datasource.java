package org.wildfly.swarm.datasources;

import org.jboss.as.controller.PathAddress;
import org.jboss.dmr.ModelNode;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ADD;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP_ADDR;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Bob McWhirter
 */
public class Datasource {

    private final String name;
    private String connectionUrl;
    private String driverName;

    private String userName;
    private String password;

    public Datasource(String name) {
        this.name = name;
    }

    public Datasource connectionURL(String connectionUrl) {
        this.connectionUrl = connectionUrl;
        return this;
    }

    public Datasource driver(String driverName) {
        this.driverName = driverName;
        return this;
    }

    public Datasource authentication(String userName, String password) {
        this.userName = userName;
        this.password = password;
        return this;
    }

    ModelNode get(PathAddress address) {
        ModelNode node = new ModelNode();
        node.get( OP_ADDR ).set( address.append( "data-source", this.name ).toModelNode() );
        node.get( OP ).set( ADD );

        node.get( "enabled" ).set( true );
        node.get( "jndi-name" ).set( "java:jboss/datasources/" + this.name );
        node.get( "use-java-context" ).set( true );
        node.get( "connection-url" ).set( this.connectionUrl );
        node.get( "driver-name" ).set( this.driverName );

        if ( this.userName != null ) {
            node.get( "user-name" ).set( this.userName );
        }

        if ( this.password != null ) {
            node.get( "password" ).set( this.password );
        }

        return node;
    }

}
