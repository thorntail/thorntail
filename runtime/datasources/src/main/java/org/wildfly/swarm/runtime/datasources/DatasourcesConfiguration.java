package org.wildfly.swarm.runtime.datasources;

import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.PathElement;
import org.jboss.dmr.ModelNode;
import org.wildfly.swarm.datasources.Datasource;
import org.wildfly.swarm.datasources.DatasourcesFraction;
import org.wildfly.swarm.datasources.Driver;
import org.wildfly.swarm.runtime.container.AbstractServerConfiguration;

import java.util.ArrayList;
import java.util.List;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ADD;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP_ADDR;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.SUBSYSTEM;

/**
 * @author Bob McWhirter
 */
public class DatasourcesConfiguration extends AbstractServerConfiguration<DatasourcesFraction> {

    private PathAddress datasourcesAddress = PathAddress.pathAddress(PathElement.pathElement(SUBSYSTEM, "datasources"));

    public DatasourcesConfiguration() {
        super(DatasourcesFraction.class);
    }

    @Override
    public DatasourcesFraction defaultFraction() {
        return new DatasourcesFraction();
    }

    @Override
    public List<ModelNode> getList(DatasourcesFraction fraction) {

        List<ModelNode> list = new ArrayList<>();

        ModelNode node = new ModelNode();
        node.get(OP_ADDR).set(datasourcesAddress.toModelNode());
        node.get(OP).set(ADD);
        list.add(node);

        addDrivers( fraction, list );
        addDatasources( fraction, list );

        return list;
    }

    protected void addDrivers(DatasourcesFraction fraction, List<ModelNode> list) {
        for ( Driver each : fraction.drivers() ) {
            addDriver( each, list );
        }
    }

    protected void addDriver(Driver driver, List<ModelNode> list) {
        ModelNode node = new ModelNode();
        node.get(OP_ADDR).set(datasourcesAddress.append("jdbc-driver", driver.name()).toModelNode());
        node.get( OP ).set( ADD );
        node.get( "driver-name" ).set( driver.name());
        if ( driver.datasourceClassName() != null ) {
            node.get( "driver-datasource-class-name" ).set( driver.datasourceClassName() );
        }

        if ( driver.xaDatasourceClassName() != null ) {
            node.get( "driver-xa-datasource-class-name" ).set( driver.xaDatasourceClassName() );
        }

        node.get( "driver-module-name" ).set( driver.moduleName() );
        if ( driver.moduleSlot() != null ) {
            node.get( "module-slot").set( driver.moduleSlot() );
        }
    }

    protected void addDatasources(DatasourcesFraction fraction, List<ModelNode> list) {
        for (Datasource each : fraction.datasources() ) {
            addDatasource( each, list );
        }
    }

    protected void addDatasource(Datasource datasource, List<ModelNode> list) {

        ModelNode node = new ModelNode();
        node.get(OP_ADDR).set(datasourcesAddress.append("data-source", datasource.name()).toModelNode());
        node.get( OP ).set( ADD );

        node.get( "enabled" ).set( true );
        node.get( "jndi-name" ).set( datasource.jndiName() );
        node.get( "use-java-context" ).set( true );
        node.get( "connection-url" ).set( datasource.connectionURL() );
        node.get( "driver-name" ).set( datasource.driver() );

        if ( datasource.userName() != null ) {
            node.get( "user-name" ).set( datasource.userName() );
        }

        if ( datasource.password() != null ) {
            node.get( "password" ).set( datasource.password() );
        }

    }
}
