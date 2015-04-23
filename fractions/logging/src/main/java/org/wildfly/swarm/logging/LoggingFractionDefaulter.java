package org.wildfly.swarm.logging;

import org.wildfly.swarm.container.AbstractFractionDefaulter;

/**
 * @author Bob McWhirter
 */
public class LoggingFractionDefaulter extends AbstractFractionDefaulter<LoggingFraction> {

    private static final String DEFAULT_LEVEL = "INFO";

    public LoggingFractionDefaulter() {
        super( LoggingFraction.class);
    }

    @Override
    public LoggingFraction getDefaultSubsystem() {
        return new LoggingFraction()
                .formatter("PATTERN", "%d{yyyy-MM-dd HH:mm:ss,SSS} %-5p [%c] (%t) %s%e%n")
                .consoleHandler(DEFAULT_LEVEL, "PATTERN")
                .rootLogger("CONSOLE", DEFAULT_LEVEL);
    }
}
