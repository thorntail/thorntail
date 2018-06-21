package io.thorntail.vertx.impl;

import org.jboss.logging.BasicLogger;
import org.jboss.logging.Logger;
import org.jboss.logging.annotations.LogMessage;
import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.MessageLogger;
import io.thorntail.logging.impl.LoggingUtil;
import io.thorntail.logging.impl.MessageOffsets;

import static io.thorntail.logging.impl.LoggingUtil.CODE;

/**
 * Created by bob on 1/19/18.
 */
@MessageLogger(projectCode = CODE, length = 6)
public interface VertxMessages extends BasicLogger {
    VertxMessages MESSAGES = Logger.getMessageLogger(VertxMessages.class, LoggingUtil.loggerCategory("vertx"));

    int OFFSET = MessageOffsets.VERTX_OFFSET;

    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 1 + OFFSET, value = "tracing requested for vertx but tracing is not available")
    void tracingNotAvailable();

    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 2 + OFFSET, value = "tracing requested for vertx but tracing is not available")
    void tracingEnabled();

    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 3 + OFFSET, value = "deployed verticle: %s")
    void deployedVerticle(String verticleClassName);

}
