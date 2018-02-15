package org.jboss.unimbus.datasources.impl;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.sql.DataSource;

import org.jboss.jca.adapters.jdbc.JDBCResourceAdapter;
import org.jboss.jca.adapters.jdbc.WrappedConnection;
import org.jboss.jca.adapters.jdbc.WrapperDataSource;
import org.jboss.jca.adapters.jdbc.local.LocalManagedConnectionFactory;
import org.jboss.jca.common.api.metadata.common.TransactionSupportEnum;
import org.jboss.jca.common.api.metadata.spec.AdminObject;
import org.jboss.jca.common.api.metadata.spec.AuthenticationMechanism;
import org.jboss.jca.common.api.metadata.spec.ConfigProperty;
import org.jboss.jca.common.api.metadata.spec.ConnectionDefinition;
import org.jboss.jca.common.api.metadata.spec.InboundResourceAdapter;
import org.jboss.jca.common.api.metadata.spec.OutboundResourceAdapter;
import org.jboss.jca.common.api.metadata.spec.ResourceAdapter;
import org.jboss.jca.common.api.metadata.spec.SecurityPermission;
import org.jboss.jca.common.api.metadata.spec.XsdString;
import org.jboss.jca.common.metadata.spec.ConnectionDefinitionImpl;
import org.jboss.jca.common.metadata.spec.OutboundResourceAdapterImpl;
import org.jboss.jca.common.metadata.spec.ResourceAdapterImpl;
import org.jboss.unimbus.datasources.DataSourceMetaData;
import org.jboss.unimbus.jdbc.impl.JDBCDriverRegistry;

import static org.jboss.unimbus.jca.impl.Util.property;
import static org.jboss.unimbus.jca.impl.Util.str;

/**
 * Created by bob on 2/9/18.
 */
@ApplicationScoped
public class ResourceAdapterBuilder {

    ResourceAdapter build(DataSourceMetaData ds) {
        XsdString resourceAdapterClass = str(JDBCResourceAdapter.class.getName());
        List<ConfigProperty> configProperties = new ArrayList<>();
        OutboundResourceAdapter outboundResourceAdapter = outboundResourceAdapter(ds);
        InboundResourceAdapter inboundResourceAdapter = inboundResourceAdapter(ds);
        List<AdminObject> adminObjects = new ArrayList<>();
        List<SecurityPermission> securityPermissions = new ArrayList<>();
        String id = ds.getId();

        return new ResourceAdapterImpl(resourceAdapterClass,
                                       configProperties,
                                       outboundResourceAdapter,
                                       inboundResourceAdapter,
                                       adminObjects,
                                       securityPermissions,
                                       id);
    }

    private OutboundResourceAdapter outboundResourceAdapter(DataSourceMetaData ds) {
        List<ConnectionDefinition> connectionDefinition = new ArrayList<>();
        connectionDefinition.add(connectionDefinition(ds));
        TransactionSupportEnum transactionSupport = TransactionSupportEnum.LocalTransaction;
        List<AuthenticationMechanism> authenticationMechanism = new ArrayList<>();
        boolean reauthenticationSupport = false;
        String id = ds.getId();
        String transactionSupportId = null;
        String reauthenticationSupportId = null;
        OutboundResourceAdapterImpl ra = new OutboundResourceAdapterImpl(connectionDefinition,
                                                                         transactionSupport,
                                                                         authenticationMechanism,
                                                                         reauthenticationSupport,
                                                                         id,
                                                                         transactionSupportId,
                                                                         reauthenticationSupportId);

        return ra;
    }

    private ConnectionDefinition connectionDefinition(DataSourceMetaData ds) {
        XsdString managedConnectionFactoryClass = str(LocalManagedConnectionFactory.class.getName());
        List<ConfigProperty> configProperty = new ArrayList<>();
        configProperty.add(property("DriverClass", this.driverRegistry.get(ds.getDriver()).getDriverClassName()));
        configProperty.add(property("ConnectionURL", ds.getConnectionUrl()));
        configProperty.add(property("JndiName", ds.getJNDIName()));
        configProperty.add(property("UserName", ds.getUsername()));
        configProperty.add(property("Password", ds.getPassword()));

        XsdString connectionFactoryInterface = str(DataSource.class.getName());
        XsdString connectionFactoryImplClass = str(WrapperDataSource.class.getName());
        XsdString connectionInterface = str(Connection.class.getName());
        XsdString connectionImplClass = str(WrappedConnection.class.getName());
        String id = ds.getId();
        ConnectionDefinitionImpl cd = new ConnectionDefinitionImpl(
                managedConnectionFactoryClass,
                configProperty,
                connectionFactoryInterface,
                connectionFactoryImplClass,
                connectionInterface,
                connectionImplClass,
                id);

        return cd;
    }


    private InboundResourceAdapter inboundResourceAdapter(DataSourceMetaData ds) {
        return null;
    }



    @Inject
    JDBCDriverRegistry driverRegistry;
}
