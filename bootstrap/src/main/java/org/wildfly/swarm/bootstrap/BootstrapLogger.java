package org.wildfly.swarm.bootstrap;

import java.util.Date;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

/**
 * @author Bob McWhirter
 */
public class BootstrapLogger {

    private static class Manager {
        static final Manager INSTANCE = new Manager();

        private static final String PREFIX = "swarm.bootstrap.log.";

        private final Set<String> enabled = new HashSet<>();

        private Manager() {
            Properties props = System.getProperties();
            Set<String> names = props.stringPropertyNames();
            for (String name : names) {
                if ( name.startsWith( PREFIX ) ) {
                    String bit = name.substring( PREFIX.length() );
                    this.enabled.add( bit );
                }
            }
        }

        public synchronized void log(String name, String level, String message) {
            if ( ! this.enabled.contains(name) ) {
                return;
            }

            Date now = new Date();
            String[] lines = message.split("\n");

            for (String line : lines) {
                System.err.println(String.format("%s %s [%s] (%s) %s",
                        now,
                        level,
                        name,
                        Thread.currentThread().getName(),
                        line));
            }
        }


        public synchronized void log(String name, String level, Throwable t) {
            if ( ! this.enabled.contains(name) ) {
                return;
            }
            System.err.println(String.format("%s %s [%s] (%s) %s",
                    new Date().toString(),
                    level,
                    name,
                    Thread.currentThread().getName(),
                    t.getMessage()));
            for (StackTraceElement stackTraceElement : t.getStackTrace()) {
                System.err.println("  " + stackTraceElement.toString());
            }
        }

        public void error(String name, String message) {
            log(name, "ERROR", message);
        }

        public void error(String name, Throwable t) {
            log(name, "ERROR", t);
        }

        public void error(String name, String message, Throwable t) {
            log(name, "ERROR", message);
            log(name, "ERROR", t);
        }

        public void debug(String name, Object message) {
            log(name, "DEBUG", message.toString());
        }

        public void warn(String name, Object message) {
            log(name, "WARN", message.toString());
        }

        public void info(String name, Object message) {
            log(name, "INFO", message.toString());
        }
    }

    private final String name;

    private BootstrapLogger(String name) {
        this.name = name;
    }

    public static BootstrapLogger logger(String name) {
        return new BootstrapLogger(name);
    }

    public void error(String message) {
        Manager.INSTANCE.error(this.name, message);
    }

    public void error(Throwable t) {
        Manager.INSTANCE.error(this.name, t);
    }

    public void error(String message, Throwable t) {
        Manager.INSTANCE.error(this.name, message, t);
    }

    public void debug(String message) {
        Manager.INSTANCE.debug(this.name, message);
    }

    public void warn(String message) {
        Manager.INSTANCE.warn(this.name, message);
    }

    public void info(String message) {
        Manager.INSTANCE.info(this.name, message);
    }
}
