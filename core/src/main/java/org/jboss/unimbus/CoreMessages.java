package org.jboss.unimbus;

import org.jboss.logging.BasicLogger;
import org.jboss.logging.Logger;
import org.jboss.logging.annotations.LogMessage;
import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.MessageLogger;

import static org.jboss.unimbus.UNimbus.PROJECT_CODE;
import static org.jboss.unimbus.UNimbus.PROJECT_NAME;

/**
 * Created by bob on 1/19/18.
 */
@MessageLogger(projectCode = PROJECT_CODE, length = 6)
public interface CoreMessages extends BasicLogger {
    CoreMessages MESSAGES = Logger.getMessageLogger(CoreMessages.class, "org.jboss.unimbus");

    int OFFSET = 0;

    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 1 + OFFSET, value = PROJECT_NAME + " started in %s")
    void started(String startTime);
}
