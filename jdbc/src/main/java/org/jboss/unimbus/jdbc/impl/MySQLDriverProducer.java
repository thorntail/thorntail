package org.jboss.unimbus.jdbc.impl;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

import org.jboss.unimbus.condition.annotation.RequiredClassPresent;
import org.jboss.unimbus.jdbc.DriverMetaData;

@ApplicationScoped
@RequiredClassPresent("com.mysql.jdbc.Driver")
public class MySQLDriverProducer {

    @Produces
    @ApplicationScoped
    DriverMetaData driverInfo() {
        return new DriverMetaData("mysql")
                .setDriverClassName(com.mysql.jdbc.Driver.class.getName())
                .setDataSourceClassName(com.mysql.jdbc.jdbc2.optional.MysqlDataSource.class.getName());
    }

}
