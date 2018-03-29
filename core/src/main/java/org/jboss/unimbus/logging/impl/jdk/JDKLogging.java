package org.jboss.unimbus.logging.impl.jdk;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
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

    @Override
    public void dump() {
        LogManager lm = LogManager.getLogManager();

        Enumeration<String> names = lm.getLoggerNames();

        List<String> sortedNames = new ArrayList<>();

        while (names.hasMoreElements()) {
            sortedNames.add(names.nextElement());
        }

        Collections.sort(sortedNames);

        for ( String each : sortedNames ) {
            Logger logger = lm.getLogger(each);
            Logger cur = logger;

            if ( cur == null ) {
                System.err.println( "null logger: " + each );
                break;
            }
            while ( cur.getLevel() == null ) {
                cur = cur.getParent();
            }

            if ( cur.getLevel() != null ) {
                System.err.println(String.format("%10s %s", cur.getLevel().toString(), logger.getName()));
            }
        }
    }

    @Inject
    @ConfigProperty(name = "logging.jdk.console.format")
    String format;

}
