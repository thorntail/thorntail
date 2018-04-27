package io.thorntail.security.impl;

import org.jboss.logging.BasicLogger;
import org.jboss.logging.Logger;
import org.jboss.logging.annotations.LogMessage;
import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.MessageLogger;
import io.thorntail.logging.impl.LoggingUtil;
import io.thorntail.logging.impl.MessageOffsets;

/**
 * Created by bob on 1/19/18.
 */
@MessageLogger(projectCode = LoggingUtil.CODE, length = 6)
public interface SecurityMessages extends BasicLogger {
    SecurityMessages MESSAGES = Logger.getMessageLogger(SecurityMessages.class, LoggingUtil.loggerCategory("security"));

    int OFFSET = MessageOffsets.SECURITY_OFFSET;

    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 1 + OFFSET, value = "Temporary 'admin' password: %s")
    void temporaryAdminPassword(String password);

}
