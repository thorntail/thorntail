package org.jboss.unimbus.datasources;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.sql.DataSource;

import org.jboss.unimbus.Initializer;

@ApplicationScoped
public class DataSourceInitializer implements Initializer {

    @Override
    public void initialize() {
        System.err.println( "DS initilaizer: " + ds);
    }

    @Inject
    private DataSource ds;
}
