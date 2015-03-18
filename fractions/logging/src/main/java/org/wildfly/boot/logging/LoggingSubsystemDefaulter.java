package org.wildfly.boot.logging;

import org.wildfly.boot.container.AbstractSubsystemDefaulter;

/**
 * @author Bob McWhirter
 */
public class LoggingSubsystemDefaulter extends AbstractSubsystemDefaulter<LoggingSubsystem> {

    private static final String DEFAULT_LEVEL = "INFO";

    public LoggingSubsystemDefaulter() {
        super( LoggingSubsystem.class);
    }

    @Override
    public LoggingSubsystem getDefaultSubsystem() {
        return new LoggingSubsystem()
                .formatter("PATTERN", "%d{yyyy-MM-dd HH:mm:ss,SSS} %-5p [%c] (%t) %s%e%n")
                .consoleHandler(DEFAULT_LEVEL, "PATTERN")
                .rootLogger("CONSOLE", DEFAULT_LEVEL);
    }
}
