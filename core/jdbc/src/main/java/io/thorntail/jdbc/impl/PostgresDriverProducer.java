package io.thorntail.jdbc.impl;

import io.thorntail.condition.annotation.RequiredClassPresent;
import io.thorntail.jdbc.DriverMetaData;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

@ApplicationScoped
@RequiredClassPresent("org.postgresql.Driver")
public class PostgresDriverProducer {

    @Produces
    @ApplicationScoped
    DriverMetaData driverInfo() {
        return new DriverMetaData("postgresql")
                .setDriverClassName("org.postgresql.Driver")
                .setDataSourceClassName("org.postgresql.ds.PGSimpleDataSource");
    }

}
