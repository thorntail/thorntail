package org.jboss.unimbus.datasources;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.sql.DataSource;

import org.jboss.unimbus.events.BeforeStart;

@ApplicationScoped
public class DataSourceListener {

    public void preInitialize(@Observes @BeforeStart Boolean event) {
        System.err.println("DS initilaizer: " + ds);
    }

    @Inject
    private DataSource ds;
}
