/**
 * Copyright 2015-2016 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wildfly.swarm.logging;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.wildfly.swarm.config.Logging;
import org.wildfly.swarm.config.logging.AsyncHandler;
import org.wildfly.swarm.config.logging.ConsoleHandler;
import org.wildfly.swarm.config.logging.CustomFormatter;
import org.wildfly.swarm.config.logging.CustomHandler;
import org.wildfly.swarm.config.logging.FileHandler;
import org.wildfly.swarm.config.logging.Level;
import org.wildfly.swarm.config.logging.PatternFormatter;
import org.wildfly.swarm.config.logging.RootLogger;
import org.wildfly.swarm.config.logging.SyslogHandler;
import org.wildfly.swarm.spi.api.Fraction;
import org.wildfly.swarm.spi.api.annotations.MarshalDMR;
import org.wildfly.swarm.spi.api.annotations.WildFlyExtension;

/**
 * @author Bob McWhirter
 * @author Ken Finnigan
 * @author Lance Ball
 */
@SuppressWarnings("unused")
@WildFlyExtension(module = "org.jboss.as.logging")
@MarshalDMR
public class LoggingFraction extends Logging<LoggingFraction> implements Fraction<LoggingFraction> {

    public static final String CONSOLE = "CONSOLE";

    public static final String PATTERN = "PATTERN";

    public static final String COLOR_PATTERN = "COLOR_PATTERN";


    public LoggingFraction applyDefaults() {
        Level level = Level.INFO;

        String prop = System.getProperty(LoggingProperties.LOGGING);
        if (prop != null) {
            prop = prop.trim().toUpperCase();
            try {
                level = Level.valueOf(prop);
            } catch (IllegalArgumentException e) {
                // Go with default of Level.INFO
            }
        }

        return applyDefaults(level);
    }

    public LoggingFraction applyDefaults(Level level) {
        defaultColorFormatter()
                .consoleHandler(Level.ALL, COLOR_PATTERN)
                .rootLogger(level, CONSOLE);

        Properties allProps = System.getProperties();
        for (String name : allProps.stringPropertyNames()) {
            if (isSimpleLoggerName(name)) {
                String logger = name.substring((LoggingProperties.LOGGING + ".").length());
                try {
                    Level loggerLevel = Level.valueOf(allProps.getProperty(name).trim().toUpperCase());
                    logger(logger, (l) -> {
                        l.level(loggerLevel);
                        l.category(logger);
                        l.handler(CONSOLE);
                    });
                } catch (IllegalArgumentException e) {
                    // apparently wasn't a logging category+level, ignore.
                }
            }
        }


        return this;
    }

    protected boolean isSimpleLoggerName(String name) {
        if (!name.startsWith(LoggingProperties.LOGGING + ".")) {
            return false;
        }

        if (name.endsWith("level")) {
            return false;
        }

        if (name.matches("^.*\\.handlers.[0-9]+$")) {
            return false;
        }

        return true;
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
    public static LoggingFraction createDefaultLoggingFraction(Level level) {
        return new LoggingFraction().applyDefaults(level);
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
        return formatter(COLOR_PATTERN, "%K{level}%d{yyyy-MM-dd HH:mm:ss,SSS} %-5p [%c] (%t) %s%e%n");
    }


    /**
     * Add a new PatternFormatter to this Logger
     *
     * @param name    the name of the formatter
     * @param pattern the pattern string
     * @return This fraction.
     */
    public LoggingFraction formatter(String name, String pattern) {
        patternFormatter(new PatternFormatter(name).pattern(pattern));
        return this;
    }

    /**
     * Add a CustomFormatter to this logger
     *
     * @param name      the name of the formatter
     * @param module    the module that the logging handler depends on
     * @param className the logging handler class to be used
     * @return This fraction.
     */
    public LoggingFraction customFormatter(String name, String module, String className) {
        return customFormatter(name, module, className, null);
    }

    /**
     * Add a CustomFormatter to this logger
     *
     * @param name       the name of the formatter
     * @param module     the module that the logging handler depends on
     * @param className  the logging handler class to be used
     * @param properties the properties
     * @return this fraction
     */
    public LoggingFraction customFormatter(String name, String module, String className, Properties properties) {
        Map<Object, Object> formatterProperties = new HashMap<>();
        final Enumeration<?> names = properties.propertyNames();
        while (names.hasMoreElements()) {
            final String nextElement = (String) names.nextElement();
            formatterProperties.put(nextElement, properties.getProperty(nextElement));
        }
        customFormatter(new CustomFormatter(name)
                .module(module)
                .attributeClass(className)
                .properties(formatterProperties));
        return this;
    }

    /**
     * Get the list of PatternFormatter configurations
     *
     * @return The list of formatters
     */
    public List<PatternFormatter> patternFormatters() {
        return subresources().patternFormatters();
    }

    /**
     * Get the list of CustomFormatter configurations
     *
     * @return The list of custom formatters
     */
    public List<CustomFormatter> customFormatters() {
        return subresources().customFormatters();
    }

    // ---------- HANDLERS ----------

    /**
     * Add a ConsoleHandler to the list of handlers for this logger.
     *
     * @param level     The logging level
     * @param formatter A pattern string for the console's formatter
     * @return This fraction
     */
    public LoggingFraction consoleHandler(Level level, String formatter) {
        consoleHandler(new ConsoleHandler(CONSOLE)
                .level(level)
                .namedFormatter(formatter));
        return this;
    }

    /**
     * Get the list of ConsoleHandlers for this logger
     *
     * @return the list of handlers
     */
    public List<ConsoleHandler> consoleHandlers() {
        return subresources().consoleHandlers();
    }

    /**
     * Add a FileHandler to the list of handlers for this logger
     *
     * @param name      The name of the handler
     * @param path      The log file path
     * @param level     The logging level
     * @param formatter The pattern string for the formatter
     * @return This fraction
     */
    public LoggingFraction fileHandler(String name, String path, Level level, String formatter) {
        Map<Object, Object> fileProperties = new HashMap<>();
        fileProperties.put("path", path);
        fileProperties.put("relative-to", "jboss.server.log.dir");
        fileHandler(new FileHandler(name)
                .level(level)
                .formatter(formatter)
                .file(fileProperties));
        return this;
    }

    /**
     * Get the list of FileHandlers configured on this logger
     *
     * @return the list of FileHandlers
     */
    public List<FileHandler> fileHandlers() {
        return subresources().fileHandlers();
    }

    /**
     * Add a CustomHandler to this logger
     *
     * @param name       the name of the handler
     * @param module     the module that the handler uses
     * @param className  the handler class name
     * @param properties properties for the handler
     * @param formatter  a pattern string for the formatter
     * @return this fraction
     */
    public LoggingFraction customHandler(String name, String module, String className, Properties properties, String formatter) {
        Map<Object, Object> handlerProperties = new HashMap<>();
        final Enumeration<?> names = properties.propertyNames();
        while (names.hasMoreElements()) {
            final String nextElement = (String) names.nextElement();
            handlerProperties.put(nextElement, properties.getProperty(nextElement));
        }

        customHandler(new CustomHandler(name)
                .module(module)
                .attributeClass(className)
                .formatter(formatter)
                .properties(handlerProperties));
        return this;
    }

    /**
     * Get the list of CustomHandlers for this logger
     *
     * @return the list of handlers
     */
    public List<CustomHandler> customHandlers() {
        return subresources().customHandlers();
    }

    /**
     * Get the list of AsyncHandlers for this logger
     *
     * @return the list of handlers
     */
    public List<AsyncHandler> asyncHandlers() {
        return subresources().asyncHandlers();
    }

    /**
     * Get the list of SyslogHandlers for this logger
     *
     * @return the list of handlers
     */
    public List<SyslogHandler> syslogHandlers() {
        return subresources().syslogHandlers();
    }

    // TODO: Add methods for PeriodicRotatingFileHandler, PeriodicSizeRotatingFileHandler, SizeRotatingFileHandler

    // -------- ROOT logger ---------

    /**
     * Add a root logger to this fraction
     *
     * @param level    the log level
     * @param handlers a list of handlers
     * @return this fraction
     */
    public LoggingFraction rootLogger(Level level, String... handlers) {
        rootLogger(new RootLogger().level(level)
                .handlers(handlers));
        return this;
    }

}
