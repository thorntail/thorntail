package io.thorntail.datasources.impl;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.sql.DataSource;

import io.thorntail.datasources.impl.opentracing.TracedLocalManagedConnectionFactory;
import io.thorntail.jca.impl.Util;
import org.eclipse.microprofile.config.Config;
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
import io.thorntail.datasources.DataSourceMetaData;
import io.thorntail.TraceMode;
import io.thorntail.jdbc.impl.JDBCDriverRegistry;

/**
 * Created by bob on 2/9/18.
 */
@ApplicationScoped
public class ResourceAdapterBuilder {

    ResourceAdapter build(DataSourceMetaData ds) {
        XsdString resourceAdapterClass = Util.str(JDBCResourceAdapter.class.getName());
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
        XsdString managedConnectionFactoryClass = Util.str(getManagedConnectionFactoryClassName(ds));
        List<ConfigProperty> configProperty = new ArrayList<>();
        configProperty.add(Util.property("DriverClass", this.driverRegistry.get(ds.getDriver()).getDriverClassName()));
        configProperty.add(Util.property("ConnectionURL", ds.getConnectionUrl()));
        configProperty.add(Util.property("JndiName", ds.getJNDIName()));
        configProperty.add(Util.property("UserName", ds.getUsername()));
        configProperty.add(Util.property("Password", ds.getPassword()));
        if (managedConnectionFactoryClass.getValue().equals(TracedLocalManagedConnectionFactory.class.getName())) {
            configProperty.add(Util.property("TraceMode", ds.getTraceMode().toString()));
        }

        XsdString connectionFactoryInterface = Util.str(DataSource.class.getName());
        XsdString connectionFactoryImplClass = Util.str(WrapperDataSource.class.getName());
        XsdString connectionInterface = Util.str(Connection.class.getName());
        XsdString connectionImplClass = Util.str(WrappedConnection.class.getName());
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

    private String getManagedConnectionFactoryClassName(DataSourceMetaData ds) {
        if (isTraced(ds)) {
            return TracedLocalManagedConnectionFactory.class.getName();
        } else {
            return LocalManagedConnectionFactory.class.getName();
        }
    }

    private boolean isTraced(DataSourceMetaData ds) {
        if (ds.getTraceMode() != TraceMode.OFF) {
            if (isTracingAvailable()) {
                DataSourcesMessages.MESSAGES.tracingEnabled(ds.getId());
                return true;
            } else {
                DataSourcesMessages.MESSAGES.tracingNotAvailable(ds.getId());
            }
        }

        return false;
    }

    private boolean isTracingAvailable() {
        Boolean result = this.tracingAvailable.updateAndGet((prev) -> {
            if (prev != null) {
                return prev;
            }
            try {
                Class<?> globalTracer = Class.forName("io.opentracing.util.GlobalTracer");
                if ( globalTracer != null ) {
                    return Boolean.TRUE;
                }
            } catch (ClassNotFoundException e) {
                // ignore, false will follow.
            }
            return Boolean.FALSE;
        });
        return result;
    }


    @Inject
    JDBCDriverRegistry driverRegistry;

    @Inject
    Config config;

    private AtomicReference<Boolean> tracingAvailable = new AtomicReference<>(null);
}
