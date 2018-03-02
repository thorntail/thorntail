package org.jboss.unimbus.impl;

import org.jboss.logging.BasicLogger;
import org.jboss.logging.Logger;
import org.jboss.logging.annotations.LogMessage;
import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.MessageLogger;
import org.jboss.unimbus.logging.impl.LoggingUtil;
import org.jboss.unimbus.logging.impl.MessageOffsets;

import static org.jboss.unimbus.logging.impl.LoggingUtil.CODE;
import static org.jboss.unimbus.Info.NAME;

/**
 * Created by bob on 1/19/18.
 */
@MessageLogger(projectCode = CODE, length = 6)
public interface CoreMessages extends BasicLogger {
    CoreMessages MESSAGES = Logger.getMessageLogger(CoreMessages.class, LoggingUtil.loggerCategory("core"));

    int OFFSET = MessageOffsets.CORE_OFFSET;

    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 1 + OFFSET, value = NAME + " - version %s")
    void versionInfo(String version);

    // --

    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 10 + OFFSET, value = NAME + " starting")
    void starting();

    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 11 + OFFSET, value = NAME + " stopping")
    void stopping();

    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 12 + OFFSET, value = NAME + " stopped")
    void stopped();

    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 13 + OFFSET, value = "phase [%s] completed in %s")
    void timing(String phase, String time);

    // --

    @LogMessage(level = Logger.Level.WARN)
    @Message(id = 20 + OFFSET, value = "Unable to process YAML configuration %s. Add snakeyaml to your dependencies to enable")
    void unableToProcessYaml(String url);

    // --

    @LogMessage(level = Logger.Level.WARN)
    @Message(id = 30 + OFFSET, value = "No valid OpenTracing Tracer resolved")
    void noValidTracer();

    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 31 + OFFSET, value = "Registered OpenTracing Tracer '%s'")
    void registeredTracer(String tracerClass);

    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 999 + OFFSET, value = NAME + " started in %s")
    void started(String startTime);
}
