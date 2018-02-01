package org.jboss.unimbus.jdbc;

import java.sql.Driver;
import java.sql.SQLException;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Named;

import org.jboss.unimbus.condition.RequiredClassPresent;

@ApplicationScoped
@RequiredClassPresent("org.h2.Driver")
public class H2DriverProducer {

    /*
    @Produces
    @ApplicationScoped
    @Named("jdbc.driver.h2")
    Driver driver() throws SQLException {
        return new org.h2.Driver();
    }
    */

    @Produces
    @ApplicationScoped
    DriverInfo driverInfo() {
        System.err.println("PRODUCE h2 driver info");
        return new DriverInfo("h2")
                .setDriverClassName(org.h2.Driver.class.getName())
                .setDataSourceClassName(org.h2.jdbcx.JdbcDataSource.class.getName());
    }

}
