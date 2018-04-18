package io.thorntail.jdbc.impl;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

import io.thorntail.jdbc.DriverMetaData;
import io.thorntail.condition.annotation.RequiredClassPresent;

@ApplicationScoped
@RequiredClassPresent("org.h2.Driver")
public class H2DriverProducer {

    @Produces
    @ApplicationScoped
    DriverMetaData driverInfo() {
        return new DriverMetaData("h2")
                .setDriverClassName(org.h2.Driver.class.getName())
                .setDataSourceClassName(org.h2.jdbcx.JdbcDataSource.class.getName());
    }

}
