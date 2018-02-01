package org.jboss.unimbus.datasources;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.resource.ResourceException;
import javax.resource.spi.ConnectionManager;
import javax.resource.spi.ManagedConnectionFactory;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.jca.adapters.jdbc.JDBCResourceAdapter;
import org.jboss.jca.adapters.jdbc.local.LocalManagedConnectionFactory;
import org.jboss.jca.adapters.jdbc.xa.XAManagedConnectionFactory;
import org.jboss.unimbus.events.LifecycleEvent;
import org.jboss.unimbus.jdbc.DriverInfo;
import org.jboss.unimbus.jdbc.DriverRegistry;

/**
 * Created by bob on 1/31/18.
 */
@ApplicationScoped
public class ManagedConnectionFactoryProducer {

    void start(@Observes LifecycleEvent.Initialize event) {
        System.err.println( "CONNECTION FACTORY PRODUCER INIT");


    }

    @Produces
    ManagedConnectionFactory managedConnectionFactory() throws ResourceException {
        LocalManagedConnectionFactory factory = new LocalManagedConnectionFactory();
        factory.setDriverClass(getDriverInfo().getDriverClassName());
        factory.setResourceAdapter( new JDBCResourceAdapter() );
        factory.setConnectionURL(this.connectionUrl);
        factory.setUserName(this.username);
        factory.setPassword(this.password);
        //factory.setDataSourceClass( this.driverInfo.getDataSourceClassName() );
        return factory;
    }

    DriverInfo getDriverInfo() {
        return this.driverRegistry.get(this.driver);
    }


    @Inject
    DriverRegistry driverRegistry;

    @Inject
    @ConfigProperty(name = "datasource.username")
    String username;

    @Inject
    @ConfigProperty(name = "datasource.password")
    String password;

    @Inject
    @ConfigProperty(name = "datasource.driver")
    String driver;

    @Inject
    @ConfigProperty(name = "datasource.connection.url")
    String connectionUrl;
}
