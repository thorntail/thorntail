package org.wildfly.swarm.logging;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import org.wildfly.swarm.container.Fraction;
import org.wildfly.swarm.logging.format.CustomFormatter;
import org.wildfly.swarm.logging.format.Formatter;
import org.wildfly.swarm.logging.format.PatternFormatter;
import org.wildfly.swarm.logging.handlers.ConsoleHandler;
import org.wildfly.swarm.logging.handlers.CustomHandler;
import org.wildfly.swarm.logging.handlers.FileHandler;
import org.wildfly.swarm.logging.handlers.Handler;

/**
 * @author Bob McWhirter
 * @author Ken Finnigan
 */
public class LoggingFraction implements Fraction {

    private Map<String, Formatter> formatters = new HashMap<>();

    private ConsoleHandler consoleHandler;

    private Map<String, Handler> handlers = new HashMap<>();

    private RootLogger rootLogger;

    public LoggingFraction() {
    }

    /**
     * Create a default TRACE logging fraction.
     *
     * @return The fully-configured fraction.
     */
    public static LoggingFraction createTraceLoggingFraction() {
        return createDefaultLoggingFraction("TRACE");
    }

    /**
     * Create a default DEBUG logging fraction.
     *
     * @return The fully-configured fraction.
     */
    public static LoggingFraction createDebugLoggingFraction() {
        return createDefaultLoggingFraction("DEBUG");
    }

    /**
     * Create a default ERROR logging fraction.
     *
     * @return The fully-configured fraction.
     */
    public static LoggingFraction createErrorLoggingFraction() {
        return createDefaultLoggingFraction("ERROR");
    }

    /**
     * Create a default INFO logging fraction.
     *
     * @return The fully-configured fraction.
     */
    public static LoggingFraction createDefaultLoggingFraction() {
        return createDefaultLoggingFraction("INFO");
    }

    /**
     * Create a default logging fraction for the specified level.
     *
     * @return The fully-configured fraction.
     */
    public static LoggingFraction createDefaultLoggingFraction(String level) {
        return new LoggingFraction()
                .defaultColorFormatter()
                .consoleHandler(level, "COLOR_PATTERN")
                .rootLogger(level, "CONSOLE");
    }

    public LoggingFraction formatter(String name, String pattern) {
        this.formatters.put(name, new PatternFormatter(name, pattern));
        return this;
    }

    public LoggingFraction customFormatter(String name, String module, String className) {
        this.formatters.put(name, new CustomFormatter(name, module, className));
        return this;
    }

    public LoggingFraction customFormatter(String name, String module, String className, Properties properties) {
        this.formatters.put(name, new CustomFormatter(name, module, className, properties));
        return this;
    }

    /**
     * Configure a default non-color formatter named {@code PATTERN}.
     *
     * @return This fraction.
     */
    public LoggingFraction defaultFormatter() {
        return formatter("PATTERN", "%d{yyyy-MM-dd HH:mm:ss,SSS} %-5p [%c] (%t) %s%e%n");
    }

    /**
     * Configure a default color formatter named {@code COLOR_PATTERN}.
     *
     * @return This fraction.
     */
    public LoggingFraction defaultColorFormatter() {
        return formatter("COLOR_PATTERN", "%d{yyyy-MM-dd HH:mm:ss,SSS} %-5p [%c] (%t) %s%e%n");
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

    public LoggingFraction fileHandler(String name, String path, String level, String formatter) {
        this.handlers.put(name, new FileHandler(name, path, level, formatter));
        return this;
    }

    public LoggingFraction customHandler(String name, String module, String className, Properties properties, String formatter) {
        this.handlers.put(name, new CustomHandler(name, module, className, properties, formatter));
        return this;
    }

    public List<Handler> handlers() {
        return this.handlers.values().stream().collect(Collectors.toList());
    }

    public LoggingFraction rootLogger(String level) {
        this.rootLogger = new RootLogger(level, this.handlers.keySet().toArray(new String[this.handlers.size()]));
        return this;
    }

    public LoggingFraction rootLogger(String level, String... handlers) {
        this.rootLogger = new RootLogger(level, handlers);
        return this;
    }

    public RootLogger rootLogger() {
        return this.rootLogger;
    }

}
