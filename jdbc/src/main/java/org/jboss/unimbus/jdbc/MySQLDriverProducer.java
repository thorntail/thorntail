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
    DriverInfo driverInfo() {
        return new DriverInfo("mysql")
                .setDriverClassName(com.mysql.jdbc.Driver.class.getName())
                .setDataSourceClassName(com.mysql.jdbc.jdbc2.optional.MysqlDataSource.class.getName());
    }

}
