package org.wildfly.swarm.datasources;

import org.jboss.as.controller.PathAddress;
import org.jboss.dmr.ModelNode;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ADD;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP_ADDR;

/**
 * @author Bob McWhirter
 */
public class Driver {

    private final String name;
    private String moduleName;
    private String moduleSlot;
    private String datasourceClassName;
    private String xaDatasourceClassName;

    public Driver(String name) {
        this.name = name;
    }

    public Driver module(String moduleName) {
        this.moduleName = moduleName;
        return this;
    }

    public Driver module(String moduleName, String moduleSlot) {
        this.moduleName = moduleName;
        this.moduleSlot = moduleSlot;
        return this;
    }

    public Driver datasourceClassName(String className) {
        this.datasourceClassName = className;
        return this;
    }

    public Driver xaDatasourceClassName(String className) {
        this.xaDatasourceClassName = className;
        return this;
    }

    ModelNode get(PathAddress address) {

        ModelNode node = new ModelNode();
        node.get( OP_ADDR).set( address.append( "jdbc-driver", this.name ).toModelNode() );
        node.get( OP ).set( ADD );
        node.get( "driver-name" ).set( this.name );
        if ( this.datasourceClassName != null ) {
            node.get( "driver-datasource-class-name" ).set( this.datasourceClassName );
        }

        if ( this.xaDatasourceClassName != null ) {
            node.get( "driver-xa-datasource-class-name" ).set( this.xaDatasourceClassName );
        }

        node.get( "driver-module-name" ).set( this.moduleName );
        if ( this.moduleSlot != null ) {
            node.get( "module-slot").set( this.moduleSlot );
        }

        return node;
    }
}
