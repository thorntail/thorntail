package org.jboss.unimbus.security;

import org.jboss.logging.BasicLogger;
import org.jboss.logging.Logger;
import org.jboss.logging.annotations.LogMessage;
import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.MessageLogger;
import org.jboss.unimbus.MessageOffsets;
import org.jboss.unimbus.UNimbus;

/**
 * Created by bob on 1/19/18.
 */
@MessageLogger(projectCode = UNimbus.PROJECT_CODE, length = 6)
public interface SecurityMessages extends BasicLogger {
    SecurityMessages MESSAGES = Logger.getMessageLogger(SecurityMessages.class, UNimbus.loggerCategory("security"));

    int OFFSET = MessageOffsets.SECURITY_OFFSET;

    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 1 + OFFSET, value = "Temporary 'admin' password: %s")
    void temporaryAdminPassword(String password);

}
