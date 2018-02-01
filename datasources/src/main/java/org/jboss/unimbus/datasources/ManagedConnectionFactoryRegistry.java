package org.jboss.unimbus.datasources;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.resource.spi.ManagedConnectionFactory;

import org.jboss.jca.adapters.jdbc.JDBCResourceAdapter;
import org.jboss.jca.adapters.jdbc.local.LocalManagedConnectionFactory;
import org.jboss.unimbus.jdbc.DriverInfo;
import org.jboss.unimbus.jdbc.JDBCDriverRegistry;

/**
 * Created by bob on 1/31/18.
 */
@ApplicationScoped
public class ManagedConnectionFactoryRegistry {

    @PostConstruct
    void init() {
        for (DataSourceMetaData dataSourceMetaData : this.dataSourceRegistry) {
            init( dataSourceMetaData );
        }
    }

    void init(DataSourceMetaData ds) {
        DriverInfo driver = this.JDBCDriverRegistry.get(ds.getDriver());
        LocalManagedConnectionFactory factory = new LocalManagedConnectionFactory();
        factory.setDriverClass(driver.getDriverClassName());
        factory.setResourceAdapter( new JDBCResourceAdapter() );
        factory.setConnectionURL(ds.getConnectionUrl());
        factory.setUserName(ds.getUsername());
        factory.setPassword(ds.getPassword());
        register( ds.getId(), factory );
    }

    public void register(String id, ManagedConnectionFactory factory) {
        this.factories.put( id, factory );
    }

    public Map<String, ManagedConnectionFactory> getFactories() {
        return Collections.unmodifiableMap( this.factories );
    }

    public ManagedConnectionFactory get(String id) {
        return this.factories.get(id);
    }


    @Inject
    JDBCDriverRegistry JDBCDriverRegistry;

    @Inject
    DataSourceRegistry dataSourceRegistry;

    private Map<String, ManagedConnectionFactory> factories = new HashMap<>();

}
