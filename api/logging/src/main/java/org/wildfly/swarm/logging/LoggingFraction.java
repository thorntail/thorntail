package org.wildfly.swarm.logging;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.wildfly.swarm.container.Fraction;

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

    /** Configure a default non-color formatter named {@code PATTERN}.
     *
     * @return This fraction.
     */
    public LoggingFraction defaultFormatter() {
        return formatter("PATTERN", "%d{yyyy-MM-dd HH:mm:ss,SSS} %-5p [%c] (%t) %s%e%n" );
    }

    /** Configure a default color formatter named {@code COLOR_PATTERN}.
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

    public LoggingFraction rootLogger(String handler, String level) {
        this.rootLogger = new RootLogger(handler, level);
        return this;
    }

    public RootLogger rootLogger() {
        return this.rootLogger;
    }

    /** Create a default TRACE logging fraction.
     *
     * @return The fully-configured fraction.
     */
    public static LoggingFraction createTraceLoggingFraction() {
        return createDefaultLoggingFraction("TRACE");
    }

    /** Create a default DEBUG logging fraction.
     *
     * @return The fully-configured fraction.
     */
    public static LoggingFraction createDebugLoggingFraction() {
        return createDefaultLoggingFraction("DEBUG");
    }

    /** Create a default ERROR logging fraction.
     *
     * @return The fully-configured fraction.
     */
    public static LoggingFraction createErrorLoggingFraction() {
        return createDefaultLoggingFraction("ERROR");
    }

    /** Create a default INFO logging fraction.
     *
     * @return The fully-configured fraction.
     */
    public static LoggingFraction createDefaultLoggingFraction() {
        return createDefaultLoggingFraction("INFO");
    }

    /** Create a default logging fraction for the specified level.
     *
     * @return The fully-configured fraction.
     */
    public static LoggingFraction createDefaultLoggingFraction(String level) {
        return new LoggingFraction()
                .defaultColorFormatter()
                .consoleHandler(level, "COLOR_PATTERN")
                .rootLogger("CONSOLE", level);
    }

}
