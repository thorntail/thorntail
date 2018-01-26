package org.jboss.unimbus.jaxrs.resteasy;

import org.jboss.logging.BasicLogger;
import org.jboss.logging.Logger;
import org.jboss.logging.annotations.LogMessage;
import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.MessageLogger;
import org.jboss.unimbus.MessageOffsets;
import org.jboss.unimbus.UNimbus;

/**
 * @author Ken Finnigan
 */
@MessageLogger(projectCode = UNimbus.PROJECT_CODE)
public interface JaxrsMessages extends BasicLogger {
    JaxrsMessages MESSAGES = Logger.getMessageLogger(JaxrsMessages.class, "org.jboss.unimbus.jaxrs");

    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 1 + MessageOffsets.JAXRS_OFFSET, value = "Deployment created for %s")
    void deploymentCreated(String deploymentName);
}
