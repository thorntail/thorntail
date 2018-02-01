package org.jboss.unimbus.jdbc;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

import org.jboss.unimbus.condition.RequiredClassPresent;

@ApplicationScoped
@RequiredClassPresent("org.h2.Driver")
public class H2DriverProducer {

    @Produces
    @ApplicationScoped
    DriverInfo driverInfo() {
        return new DriverInfo("h2")
                .setDriverClassName(org.h2.Driver.class.getName())
                .setDataSourceClassName(org.h2.jdbcx.JdbcDataSource.class.getName());
    }

}
