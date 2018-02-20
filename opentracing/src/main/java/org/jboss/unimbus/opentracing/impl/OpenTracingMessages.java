package org.jboss.unimbus.opentracing.impl;

import org.jboss.logging.BasicLogger;
import org.jboss.logging.Logger;
import org.jboss.logging.annotations.LogMessage;
import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.MessageLogger;
import org.jboss.unimbus.logging.impl.LoggingUtil;
import org.jboss.unimbus.logging.impl.MessageOffsets;

import static org.jboss.unimbus.Info.NAME;
import static org.jboss.unimbus.logging.impl.LoggingUtil.CODE;

/**
 * Created by bob on 1/19/18.
 */
@MessageLogger(projectCode = CODE, length = 6)
public interface OpenTracingMessages extends BasicLogger {
    OpenTracingMessages MESSAGES = Logger.getMessageLogger(OpenTracingMessages.class, LoggingUtil.loggerCategory("opentracing"));

    int OFFSET = MessageOffsets.OPENTRACING_OFFSET;

    @LogMessage(level = Logger.Level.WARN)
    @Message(id = 1 + OFFSET, value = "No valid OpenTracing Tracer resolved")
    void noValidTracer();

    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 2 + OFFSET, value = "Registered OpenTracing Tracer '%s'")
    void registeredTracer(String tracerClass);

    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 10 + OFFSET, value = "Set up JAX-RS client for tracing")
    void setUpJaxRsClient();
}
