package io.thorntail.jaxrs.impl;

import org.jboss.logging.BasicLogger;
import org.jboss.logging.Logger;
import org.jboss.logging.annotations.Cause;
import org.jboss.logging.annotations.LogMessage;
import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.MessageLogger;
import io.thorntail.logging.impl.LoggingUtil;
import io.thorntail.logging.impl.MessageOffsets;

/**
 * @author Ken Finnigan
 */
@MessageLogger(projectCode = LoggingUtil.CODE)
public interface JaxrsMessages extends BasicLogger {
    JaxrsMessages MESSAGES = Logger.getMessageLogger(JaxrsMessages.class, LoggingUtil.loggerCategory("jaxrs"));

    int OFFSET = MessageOffsets.JAXRS_OFFSET;

    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 1 + OFFSET, value = "Deployment created for %s")
    void deploymentCreated(String deploymentName);

    @LogMessage(level = Logger.Level.ERROR)
    @Message(id = 10 + OFFSET, value = "An error occurred while handling request")
    void requestException(@Cause Throwable t);
}
