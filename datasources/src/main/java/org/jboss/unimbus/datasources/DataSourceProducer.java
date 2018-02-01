package org.jboss.unimbus.datasources;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.resource.ResourceException;
import javax.resource.spi.ConnectionManager;
import javax.resource.spi.ManagedConnectionFactory;
import javax.sql.DataSource;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.unimbus.jdbc.DriverInfo;
import org.jboss.unimbus.jdbc.DriverRegistry;
import org.jboss.unimbus.jndi.Binder;

/**
 * Created by bob on 1/31/18.
 */
@ApplicationScoped
public class DataSourceProducer {

    @Produces
    DataSource dataSource() throws ResourceException {
        return (DataSource) factory.createConnectionFactory(this.connectionManager);
    }

    @Produces
    Binder<DataSource> dataSourceBinder() {
        return new Binder<DataSource>(this.jndiName) {
            @Override
            public DataSource produce() throws Exception {
                return dataSource();
            }
        };
    }

    @Inject
    @ConfigProperty(name="datasource.jndi-name")
    String jndiName;

    @Inject
    ManagedConnectionFactory factory;

    @Inject
    ConnectionManager connectionManager;
}
