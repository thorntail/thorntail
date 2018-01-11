package org.jboss.unimbus.jdbc;

import java.sql.Driver;
import java.sql.SQLException;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Named;

import org.jboss.unimbus.condition.IfClassPresent;

@ApplicationScoped
@IfClassPresent("com.mysql.jdbc.Driver")
public class MySQLDriverProducer {

    @Produces
    @ApplicationScoped
    @Named("jdbc.driver.mysql")
    Driver driver() throws SQLException {
        return new com.mysql.jdbc.Driver();
    }
}
