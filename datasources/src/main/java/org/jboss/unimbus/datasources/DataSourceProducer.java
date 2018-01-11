package org.jboss.unimbus.datasources;

import java.beans.PropertyVetoException;
import java.sql.Driver;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import com.mchange.v2.c3p0.ComboPooledDataSource;

@ApplicationScoped
public class DataSourceProducer {

    @Produces
    @ApplicationScoped
    DataSource dataSource() throws PropertyVetoException, NamingException {
        ComboPooledDataSource ds = new ComboPooledDataSource();
        ds.setDriverClass(this.driver.getClass().getName());
        ds.setJdbcUrl("h2:/mydb");
        ds.setUser("sa");
        ds.setPassword("sa");
        ds.setMinPoolSize(0);
        ds.setMaxPoolSize(5);

        this.context.bind( "java:jboss/datasources/ExampleDS", ds);
        return ds;
    }

    @Inject
    private InitialContext context;

    @Inject
    private Driver driver;
}
