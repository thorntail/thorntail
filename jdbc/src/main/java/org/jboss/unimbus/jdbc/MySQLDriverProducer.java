package org.jboss.unimbus.jdbc;

import java.sql.Driver;
import java.sql.SQLException;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Named;

import org.jboss.unimbus.condition.RequiredClassPresent;

@ApplicationScoped
@RequiredClassPresent("com.mysql.jdbc.Driver")
public class MySQLDriverProducer {

    @Produces
    @ApplicationScoped
    @Named("jdbc.driver.mysql")
    Driver driver() throws SQLException {
        return new com.mysql.jdbc.Driver();
    }

    @Produces
    @ApplicationScoped
    @Named("jdbc.driver-info.mysql")
    DriverInfo driverInfo() {
        return new DriverInfo(com.mysql.jdbc.Driver.class.getName());
    }

}
