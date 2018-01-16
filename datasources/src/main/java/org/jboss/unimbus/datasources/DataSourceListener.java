package org.jboss.unimbus.datasources;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.sql.DataSource;

import org.jboss.unimbus.events.LifecycleEvent;

@ApplicationScoped
public class DataSourceListener {

    public void initialize(@Observes LifecycleEvent.Initialize event) {
        System.err.println("DS initilaizer: " + ds);
    }

    @Inject
    private DataSource ds;
}
