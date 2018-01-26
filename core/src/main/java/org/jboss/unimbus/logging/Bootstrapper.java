package org.jboss.unimbus.logging;

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
public class Bootstrapper {

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
            logging.setLevel(loggerName, level);
        }
    }

    @Inject
    private Config config;

    @Inject
    @Any
    private Instance<Logging> logging;
}
