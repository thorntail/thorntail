package org.jboss.unimbus.datasources;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.resource.ResourceException;
import javax.resource.spi.ConnectionManager;
import javax.resource.spi.ManagedConnectionFactory;

import org.jboss.unimbus.events.LifecycleEvent;

/**
 * Created by bob on 1/31/18.
 */
@ApplicationScoped
public class DataSourcesInitializer {

    void init(@Observes LifecycleEvent.BeforeStart event) {
        for (DataSourceMetaData each : this.dataSourceRegistry) {
            try {
                init(each);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    void init(DataSourceMetaData ds) throws ResourceException, NamingException {
        ConnectionManager cm = this.connectionManagerRegistry.get(ds.getId());
        ManagedConnectionFactory factory = this.connectionFactoryRegistry.get(ds.getId());
        this.context.bind( ds.getJNDIName(), factory.createConnectionFactory(cm));
        DataSourcesMessages.MESSAGES.dataSourceBound(ds.getConnectionUrl(), ds.getJNDIName());
    }

    @Inject
    ManagedConnectionFactoryRegistry connectionFactoryRegistry;

    @Inject
    ConnectionManagerRegistry connectionManagerRegistry;

    @Inject
    DataSourceRegistry dataSourceRegistry;

    @Inject
    InitialContext context;
}
