package io.thorntail.jdbc.impl;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

import io.thorntail.condition.annotation.RequiredClassPresent;
import io.thorntail.jdbc.DriverMetaData;

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
