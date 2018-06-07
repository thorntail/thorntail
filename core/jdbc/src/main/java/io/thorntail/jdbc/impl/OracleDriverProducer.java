package io.thorntail.jdbc.impl;

import io.thorntail.condition.annotation.RequiredClassPresent;
import io.thorntail.jdbc.DriverMetaData;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

@ApplicationScoped
@RequiredClassPresent("oracle.jdbc.OracleDriver")
public class OracleDriverProducer {

    @Produces
    @ApplicationScoped
    DriverMetaData driverInfo() {
        return new DriverMetaData("oracle")
                .setDriverClassName("oracle.jdbc.OracleDriver")
                .setDataSourceClassName("oracle.jdbc.datasource.OracleDataSource");
    }

}
