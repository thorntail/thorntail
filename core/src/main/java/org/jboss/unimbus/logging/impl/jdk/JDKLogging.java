package org.jboss.unimbus.logging.impl.jdk;

import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.unimbus.logging.impl.Logging;

/**
 * Created by bob on 1/16/18.
 */
@ApplicationScoped
public class JDKLogging implements Logging {


    @Override
    public void initialize() {
        Logger logger = Logger.getLogger("");
        logger.setLevel(Level.ALL);
        for (Handler handler : logger.getHandlers()) {
            handler.setLevel(Level.ALL);
            if (handler instanceof ConsoleHandler) {
                handler.setFormatter(new DefaultConsoleFormatter(this.format));
            }
        }
        logger.setLevel(Level.INFO);

    }

    @Override
    public void setLevel(String name, Level level) {
        Logger logger = Logger.getLogger(name);
        logger.setLevel(level);
    }

    @Inject
    @ConfigProperty(name = "logging.jdk.console.format")
    String format;

}
