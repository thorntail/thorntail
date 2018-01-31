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
    CoreMessages MESSAGES = Logger.getMessageLogger(CoreMessages.class, UNimbus.loggerCategory("core"));

    int OFFSET = MessageOffsets.CORE_OFFSET;

    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 0 + OFFSET, value = PROJECT_NAME + " starting")
    void starting();

    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 1 + OFFSET, value = PROJECT_NAME + " started in %s")
    void started(String startTime);

    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 2 + OFFSET, value = PROJECT_NAME + " stopping")
    void stopping();

    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 3 + OFFSET, value = PROJECT_NAME + " stopped")
    void stopped();
}
