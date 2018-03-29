package org.jboss.unimbus.logging.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.eclipse.microprofile.config.Config;
import org.jboss.unimbus.events.LifecycleEvent;

/**
 * Created by bob on 1/16/18.
 */
@ApplicationScoped
public class LoggingBootstrapper {

    private static final String PREFIX = "logging.level.";

    void initialize(@Observes LifecycleEvent.Bootstrap event) {

        Logging logging = this.logging.get();
        logging.initialize();

        List<String> loggingLevelPropNames = new ArrayList<>();
        for (String each : this.config.getPropertyNames()) {
            if (each.startsWith(PREFIX)) {
                loggingLevelPropNames.add(each);
            }
        }

        for (String loggingLevelPropName : loggingLevelPropNames) {
            Level level = config.getValue(loggingLevelPropName, Level.class);
            String loggerName = loggingLevelPropName.substring(PREFIX.length());
            System.err.println( "SET: " + loggerName + "=>" + level);
            logging.setLevel(loggerName, level);
        }
    }

    void dump(@Observes LifecycleEvent.BeforeStart event) {
        System.err.println( "before start");
        this.logging.get().dump();
    }

    void dump(@Observes LifecycleEvent.AfterStart event) {
        System.err.println( "after start");
        this.logging.get().dump();
    }

    @Inject
    private Config config;

    @Inject
    @Any
    private Instance<Logging> logging;
}
