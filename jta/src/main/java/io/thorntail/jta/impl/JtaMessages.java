package io.thorntail.jta.impl;

import org.jboss.logging.BasicLogger;
import org.jboss.logging.Logger;
import org.jboss.logging.annotations.LogMessage;
import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.MessageLogger;
import io.thorntail.logging.impl.LoggingUtil;
import io.thorntail.logging.impl.MessageOffsets;

/**
 * @author Ken Finnigan
 */
@MessageLogger(projectCode = LoggingUtil.CODE)
public interface JtaMessages extends BasicLogger {
    JtaMessages MESSAGES = Logger.getMessageLogger(JtaMessages.class, LoggingUtil.loggerCategory("jta"));

    int OFFSET = MessageOffsets.JTA_OFFSET;

    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 1 + OFFSET, value = "Registering Transaction Manager in JNDI")
    void regiterTm();
}
