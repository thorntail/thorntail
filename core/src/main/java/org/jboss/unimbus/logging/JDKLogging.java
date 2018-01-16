package org.jboss.unimbus.logging;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;

/**
 * Created by bob on 1/16/18.
 */
@ApplicationScoped
public class JDKLogging implements Logging {


    @Override
    public void initialize() {
        Logger logger = Logger.getLogger("");
        logger.setLevel(Level.INFO);
        for (Handler handler : logger.getHandlers()) {
            handler.setLevel(Level.ALL);
        }

    }

    @Override
    public void setLevel(String name, Level level) {
        Logger logger = Logger.getLogger(name);
        logger.setLevel(level);
    }

}
