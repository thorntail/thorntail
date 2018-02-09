package org.jboss.unimbus.datasources;

import java.io.File;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.sql.DataSource;

import org.jboss.jca.adapters.jdbc.JDBCResourceAdapter;
import org.jboss.jca.adapters.jdbc.WrappedConnection;
import org.jboss.jca.adapters.jdbc.WrapperDataSource;
import org.jboss.jca.adapters.jdbc.local.LocalManagedConnectionFactory;
import org.jboss.jca.common.api.metadata.common.Pool;
import org.jboss.jca.common.api.metadata.common.Recovery;
import org.jboss.jca.common.api.metadata.common.Security;
import org.jboss.jca.common.api.metadata.common.TimeOut;
import org.jboss.jca.common.api.metadata.common.TransactionSupportEnum;
import org.jboss.jca.common.api.metadata.common.Validation;
import org.jboss.jca.common.api.metadata.resourceadapter.Activation;
import org.jboss.jca.common.api.metadata.resourceadapter.WorkManager;
import org.jboss.jca.common.api.metadata.spec.AdminObject;
import org.jboss.jca.common.api.metadata.spec.AuthenticationMechanism;
import org.jboss.jca.common.api.metadata.spec.ConfigProperty;
import org.jboss.jca.common.api.metadata.spec.ConnectionDefinition;
import org.jboss.jca.common.api.metadata.spec.Connector;
import org.jboss.jca.common.api.metadata.spec.Icon;
import org.jboss.jca.common.api.metadata.spec.InboundResourceAdapter;
import org.jboss.jca.common.api.metadata.spec.LicenseType;
import org.jboss.jca.common.api.metadata.spec.LocalizedXsdString;
import org.jboss.jca.common.api.metadata.spec.OutboundResourceAdapter;
import org.jboss.jca.common.api.metadata.spec.ResourceAdapter;
import org.jboss.jca.common.api.metadata.spec.SecurityPermission;
import org.jboss.jca.common.api.metadata.spec.XsdString;
import org.jboss.jca.common.metadata.resourceadapter.ActivationImpl;
import org.jboss.jca.common.metadata.spec.ConfigPropertyImpl;
import org.jboss.jca.common.metadata.spec.ConnectionDefinitionImpl;
import org.jboss.jca.common.metadata.spec.ConnectorImpl;
import org.jboss.jca.common.metadata.spec.InboundResourceAdapterImpl;
import org.jboss.jca.common.metadata.spec.OutboundResourceAdapterImpl;
import org.jboss.jca.common.metadata.spec.ResourceAdapterImpl;
import org.jboss.unimbus.events.LifecycleEvent;
import org.jboss.unimbus.jca.ResourceAdapterDeployments;
import org.jboss.unimbus.jca.ironjacamar.ResourceAdapterDeployment;
import org.jboss.unimbus.jdbc.JDBCDriverRegistry;

/**
 * Created by bob on 2/9/18.
 */
@ApplicationScoped
public class DataSourceResourceAdapterRegistrar {

    void init(@Observes LifecycleEvent.Scan event) {
        for (DataSourceMetaData each : this.registry) {
            init(each);
        }
    }

    private void init(DataSourceMetaData ds) {

        ResourceAdapterDeployment deployment = new ResourceAdapterDeployment(
                ds.getId(),
                new File(ds.getId() + ".jar"),
                connector(ds),
                activation(ds));

        this.deployments.addDeployment(deployment);
    }

    Connector connector(DataSourceMetaData ds) {
        XsdString moduleName = null;
        XsdString vendorName = null;
        XsdString eisType = null;
        XsdString resourceAdapterVersion = null;
        LicenseType license = null;
        ResourceAdapter resourceadapter = resourceAdapter(ds);
        List<XsdString> requiredWorkContexts = new ArrayList<>();
        boolean metadataComplete = true;
        List<LocalizedXsdString> description = new ArrayList<>();
        List<LocalizedXsdString> displayNames = new ArrayList<>();
        List<Icon> icons = new ArrayList<>();
        String id = ds.getId();
        ConnectorImpl connector = new ConnectorImpl(Connector.Version.V_17,
                                                    moduleName,
                                                    vendorName,
                                                    eisType,
                                                    resourceAdapterVersion,
                                                    license,
                                                    resourceadapter,
                                                    requiredWorkContexts,
                                                    metadataComplete,
                                                    description,
                                                    displayNames,
                                                    icons,
                                                    id);

        return connector;
    }

    private Activation activation(DataSourceMetaData ds) {
        String id = ds.getId();
        String archive = ds.getId() + ".jar";
        TransactionSupportEnum transactionSupport = TransactionSupportEnum.LocalTransaction;
        List<org.jboss.jca.common.api.metadata.resourceadapter.ConnectionDefinition> connectionDefinitions = new ArrayList<>();
        connectionDefinitions.add( activationConnectionDefinition( ds ) );
        List<org.jboss.jca.common.api.metadata.resourceadapter.AdminObject> adminObjects = new ArrayList<>();
        Map<String, String> configProperties = new HashMap<>();
        List<String> beanValidationGroups = new ArrayList<>();
        String bootstrapContext = "default";
        WorkManager workmanager = null;
        ActivationImpl activation = new ActivationImpl(id,
                                                       archive,
                                                       transactionSupport,
                                                       connectionDefinitions,
                                                       adminObjects,
                                                       configProperties,
                                                       beanValidationGroups,
                                                       bootstrapContext,
                                                       workmanager);

        return activation;
    }

    private org.jboss.jca.common.api.metadata.resourceadapter.ConnectionDefinition activationConnectionDefinition(DataSourceMetaData ds) {
        Map<String, String> configProperties = new HashMap<>();
        String className = null;
        String jndiName = ds.getJNDIName();
        String poolName = null;
        Boolean enabled = true;
        Boolean useJavaContext = null;
        Boolean useCcm = true;
        Boolean sharable = true;
        Boolean enlistment = true;
        Boolean connectable = true;
        Boolean tracking = false;
        String mcp = null;
        Boolean enlistmentTrace = null;
        Pool pool = null;
        TimeOut timeOut = null;
        Validation validation = null;
        Security security = null;
        Recovery recovery = null;
        Boolean isXA = null;

        org.jboss.jca.common.metadata.resourceadapter.ConnectionDefinitionImpl cd = new org.jboss.jca.common.metadata.resourceadapter.ConnectionDefinitionImpl(
                configProperties,
                className,
                jndiName,
                poolName,
                enabled,
                useJavaContext,
                useCcm,
                sharable,
                enlistment,
                connectable,
                tracking,
                mcp,
                enlistmentTrace,
                pool,
                timeOut,
                validation,
                security,
                recovery,
                isXA);


        return cd;
    }

    private ResourceAdapter resourceAdapter(DataSourceMetaData ds) {
        XsdString resourceAdapterClass = new XsdString(JDBCResourceAdapter.class.getName(), null);
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
        XsdString managedConnectionFactoryClass = new XsdString(LocalManagedConnectionFactory.class.getName(), null);
        List<ConfigProperty> configProperty = new ArrayList<>();
        configProperty.add(
                new ConfigPropertyImpl(null,
                                       new XsdString("DriverClass", null),
                                       new XsdString("java.lang.String", null),
                                       new XsdString(this.driverRegistry.get(ds.getDriver()).getDriverClassName(), null),
                                       false,
                                       false,
                                       false,
                                       "DriverClass",
                                       true,
                                       null,
                                       null,
                                       null,
                                       null)
        );

        configProperty.add(
                new ConfigPropertyImpl(null,
                                       new XsdString("ConnectionURL", null),
                                       new XsdString("java.lang.String", null),
                                       new XsdString(ds.getConnectionUrl(), null),
                                       false,
                                       false,
                                       false,
                                       "ConnectionURL",
                                       true,
                                       null,
                                       null,
                                       null,
                                       null)
        );

        configProperty.add(
                new ConfigPropertyImpl(null,
                                       new XsdString("JndiName", null),
                                       new XsdString("java.lang.String", null),
                                       new XsdString(ds.getJNDIName(), null),
                                       false,
                                       false,
                                       false,
                                       "JndiName",
                                       true,
                                       null,
                                       null,
                                       null,
                                       null)
        );

        configProperty.add(
                new ConfigPropertyImpl(null,
                                       new XsdString("UserName", null),
                                       new XsdString("java.lang.String", null),
                                       new XsdString(ds.getUsername(), null),
                                       false,
                                       false,
                                       false,
                                       "UserName",
                                       true,
                                       null,
                                       null,
                                       null,
                                       null)
        );

        configProperty.add(
                new ConfigPropertyImpl(null,
                                       new XsdString("Password", null),
                                       new XsdString("java.lang.String", null),
                                       new XsdString(ds.getPassword(), null),
                                       false,
                                       false,
                                       false,
                                       "Password",
                                       true,
                                       null,
                                       null,
                                       null,
                                       null)
        );

        XsdString connectionFactoryInterface = new XsdString(DataSource.class.getName(), null);
        XsdString connectionFactoryImplClass = new XsdString(WrapperDataSource.class.getName(), null);
        XsdString connectionInterface = new XsdString(Connection.class.getName(), null);
        XsdString connectionImplClass = new XsdString(WrappedConnection.class.getName(), null);
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
    ResourceAdapterDeployments deployments;

    @Inject
    DataSourceRegistry registry;

    @Inject
    JDBCDriverRegistry driverRegistry;
}
