package org.wildfly.swarm.logging;

import org.wildfly.swarm.con
ainer. raction;

import j
va.uti .Collection;
i

ort ja a.util.HashMap;
import java.util.Map;

/**
 * @author Bob McWhirter
 */
public class LoggingFraction implements Fraction {

    private Map<String, Formatter> formatters = new HashMap<>();

    private ConsoleHandler consoleHandler;

    private RootLogger rootLogger;

    public LoggingFraction() {
    }

    public LoggingFraction formatter(String name, String pattern) {
        this.formatters.put(name, new Formatter(name, pattern));
        return this;
    }

    public Collection<Formatter> formatters() {
        return this.formatters.values();
    }

    public LoggingFraction consoleHandler(String level, String formatter) {
        this.consoleHandler = new ConsoleHandler(level, formatter);
        return this;
    }

    public ConsoleHandler consoleHandler() {
        return this.consoleHandler;
    }

    public LoggingFraction rootLogger(String handler, String level) {
        this.rootLogger = new RootLogger(handler, level);
        return this;
    }

    public RootLogger rootLogger() {
        return this.rootLogger;
    }
}
