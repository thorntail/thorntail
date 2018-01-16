package org.jboss.unimbus.datasources;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.sql.DataSource;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import org.jboss.unimbus.jdbc.DriverInfo;
import org.jboss.unimbus.jndi.Binder;

@ApplicationScoped
public class DataSourceProducer {

    @Produces
    @ApplicationScoped
    Binder<DataSource> dataSourceBinder() {
        return new Binder<DataSource>("java:jboss/datasources/ExampleDS") {
            @Override
            public DataSource produce() throws Exception {
                ComboPooledDataSource ds = new ComboPooledDataSource();
                ds.setDriverClass(driverInfo.getDriverClassName());
                ds.setJdbcUrl("jdbc:h2:mem:");
                ds.setUser("sa");
                ds.setPassword("sa");
                ds.setMinPoolSize(0);
                ds.setMaxPoolSize(5);
                return ds;
            }
        };
    }

    @Inject
    private DriverInfo driverInfo;
}
