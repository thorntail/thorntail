package org.wildfly.swarm.logging;

import org.wildfly.swarm.config.logging.Logging;
import org.wildfly.swarm.config.logging.subsystem.asyncHandler.AsyncHandler;
import org.wildfly.swarm.config.logging.subsystem.consoleHandler.ConsoleHandler;
import org.wildfly.swarm.config.logging.subsystem.customFormatter.CustomFormatter;
import org.wildfly.swarm.config.logging.subsystem.customHandler.CustomHandler;
import org.wildfly.swarm.config.logging.subsystem.fileHandler.FileHandler;
import org.wildfly.swarm.config.logging.subsystem.patternFormatter.PatternFormatter;
import org.wildfly.swarm.config.logging.subsystem.rootLogger.Root;
import org.wildfly.swarm.config.logging.subsystem.syslogHandler.SyslogHandler;
import org.wildfly.swarm.container.Fraction;

import java.util.*;

/**
 * @author Bob McWhirter
 * @author Ken Finnigan
 * @author Lance Ball
 */
public class LoggingFraction implements Fraction {

    public static final String CONSOLE = "CONSOLE";
    public static final String PATTERN = "PATTERN";
    public static final String COLOR_PATTERN = "COLOR_PATTERN";

    public final class Level {
        public static final String TRACE = "TRACE";
        public static final String DEBUG = "DEBUG";
        public static final String ERROR = "ERROR";
        public static final String INFO = "INFO";
    }

    private final Logging logging;

    public LoggingFraction() {
        logging = new Logging();
    }

    public Logging logger() {
        return this.logging;
    }

    /**
     * Create a default TRACE logging fraction.
     *
     * @return The fully-configured fraction.
     */
    public static LoggingFraction createTraceLoggingFraction() {
        return createDefaultLoggingFraction(Level.TRACE);
    }

    /**
     * Create a default DEBUG logging fraction.
     *
     * @return The fully-configured fraction.
     */
    public static LoggingFraction createDebugLoggingFraction() {
        return createDefaultLoggingFraction(Level.DEBUG);
    }

    /**
     * Create a default ERROR logging fraction.
     *
     * @return The fully-configured fraction.
     */
    public static LoggingFraction createErrorLoggingFraction() {
        return createDefaultLoggingFraction(Level.ERROR);
    }

    /**
     * Create a default INFO logging fraction.
     *
     * @return The fully-configured fraction.
     */
    public static LoggingFraction createDefaultLoggingFraction() {
        return createDefaultLoggingFraction(Level.INFO);
    }

    /**
     * Create a default logging fraction for the specified level.
     *
     * @return The fully-configured fraction.
     */
    public static LoggingFraction createDefaultLoggingFraction(String level) {
        return new LoggingFraction()
                .defaultColorFormatter()
                .consoleHandler(level, COLOR_PATTERN)
                .rootLogger(level, CONSOLE);
    }

    // ------- FORMATTERS ---------

    /**
     * Configure a default non-color formatter named {@code PATTERN}.
     *
     * @return This fraction.
     */
    public LoggingFraction defaultFormatter() {
        return formatter(PATTERN, "%d{yyyy-MM-dd HH:mm:ss,SSS} %-5p [%c] (%t) %s%e%n");
    }

    /**
     * Configure a default color formatter named {@code COLOR_PATTERN}.
     *
     * @return This fraction.
     */
    public LoggingFraction defaultColorFormatter() {
        return formatter(COLOR_PATTERN, "%d{yyyy-MM-dd HH:mm:ss,SSS} %-5p [%c] (%t) %s%e%n");
    }


    /**
     * Add a new PatternFormatter to this Logger
     *
     * @param name the name of the formatter
     * @param pattern the pattern string
     * @return This fraction.
     */
    public LoggingFraction formatter(String name, String pattern) {
        this.logging.patternFormatter(new PatternFormatter(name).pattern(pattern));
        return this;
    }

    /**
     * Add a CustomFormatter to this logger
     *
     * @param name the name of the formatter
     * @param module the module that the logging handler depends on
     * @param className the logging handler class to be used
     * @return This fraction.
     */
    public LoggingFraction customFormatter(String name, String module, String className) {
        return customFormatter(name, module, className, null);
    }

    /**
     * Add a CustomFormatter to this logger
     *
     * @param name the name of the formatter
     * @param module the module that the logging handler depends on
     * @param className the logging handler class to be used
     * @param properties the properties
     * @return this fraction
     */
    public LoggingFraction customFormatter(String name, String module, String className, Properties properties) {
        Map formatterProperties = new HashMap<>();
        final Enumeration<?> names = properties.propertyNames();
        while (names.hasMoreElements()) {
            final String nextElement = (String) names.nextElement();
            formatterProperties.put(nextElement, properties.getProperty(nextElement));
        }
        this.logging.customFormatter(
                new CustomFormatter(name)
                        .module(module)
                        .attributeClass(className)
                        .properties(formatterProperties));
        return this;
    }

    /**
     * Get the list of PatternFormatter configurations
     * @return The list of formatters
     */
    public List<PatternFormatter> patternFormatters() {
        return this.logging.subresources().patternFormatters();
    }

    /**
     * Get the list of CustomFormatter configurations
     * @return The list of custom formatters
     */
    public List<CustomFormatter> customFormatters() {
        return this.logging.subresources().customFormatters();
    }

    // ---------- HANDLERS ----------

    /**
     * Add a ConsoleHandler to the list of handlers for this logger.
     *
     * @param level The logging level
     * @param formatter A pattern string for the console's formatter
     * @return This fraction
     */
    public LoggingFraction consoleHandler(String level, String formatter) {
        this.logging.consoleHandler(
                new ConsoleHandler(CONSOLE)
                        .level(level)
                        .namedFormatter(formatter)
        );
        return this;
    }

    /**
     * Get the list of ConsoleHandlers for this logger
     * @return the list of handlers
     */
    public List<ConsoleHandler> consoleHandlers() {
        return this.logging.subresources().consoleHandlers();
    }

    /**
     * Add a FileHandler to the list of handlers for this logger
     *
     * @param name The name of the handler
     * @param path The log file path
     * @param level The logging level
     * @param formatter The pattern string for the formatter
     * @return This fraction
     */
    public LoggingFraction fileHandler(String name, String path, String level, String formatter) {
        Map fileProperties = new HashMap<>();
        fileProperties.put("path", path);
        fileProperties.put("relative-to", "jboss.server.log.dir");
        this.logging.fileHandler(
                new FileHandler(name)
                        .level(level)
                        .formatter(formatter)
                        .file(fileProperties)
        );
        return this;
    }

    /**
     * Get the list of FileHandlers configured on this logger
     * @return the list of FileHandlers
     */
    public List<FileHandler> fileHandlers() {
        return this.logging.subresources().fileHandlers();
    }

    /**
     * Add a CustomHandler to this logger
     *
     * @param name the name of the handler
     * @param module the module that the handler uses
     * @param className the handler class name
     * @param properties properties for the handler
     * @param formatter a pattern string for the formatter
     * @return this fraction
     */
    public LoggingFraction customHandler(String name, String module, String className, Properties properties, String formatter) {
        Map handlerProperties = new HashMap<>();
        final Enumeration<?> names = properties.propertyNames();
        while (names.hasMoreElements()) {
            final String nextElement = (String) names.nextElement();
            handlerProperties.put(nextElement, properties.getProperty(nextElement));
        }

        this.logging.customHandler(
                new CustomHandler(name)
                        .module(module)
                        .attributeClass(className)
                        .formatter(formatter)
                        .properties(handlerProperties)
        );
        return this;
    }

    /**
     * Get the list of CustomHandlers for this logger
     * @return the list of handlers
     */
    public List<CustomHandler> customHandlers() {
        return this.logging.subresources().customHandlers();
    }

    /**
     * Get the list of AsyncHandlers for this logger
     * @return the list of handlers
     */
    public List<AsyncHandler> asyncHandlers() {
        return this.logging.subresources().asyncHandlers();
    }

    /**
     * Get the list of SyslogHandlers for this logger
     * @return the list of handlers
     */
    public List<SyslogHandler> syslogHandlers() {
        return this.logging.subresources().syslogHandlers();
    }

    // TODO: Add methods for PeriodicRotatingFileHandler, PeriodicSizeRotatingFileHandler, SizeRotatingFileHandler

    // -------- ROOT logger ---------

    /**
     * Add a root logger to this fraction
     * @param level the log level
     * @return this fraction
     */
    public LoggingFraction rootLogger(String level) {
        return rootLogger(level, null);
    }

    /**
     * Add a root logger to this fraction
     * @param level the log level
     * @param handlers a list of handlers
     * @return this fraction
     */
    public LoggingFraction rootLogger(String level, String... handlers) {
        this.logging.root(
                new Root().level(level)
                        .handlers(new ArrayList<>(Arrays.asList(handlers))) );
        return this;
    }

    /**
     * Get the root logger for this fraction
     * @return the Root logger
     */
    public Root rootLogger() {
        return this.logging.root();
    }

}
