package org.jboss.unimbus.jdbc;

import org.jboss.logging.BasicLogger;
import org.jboss.logging.Logger;
import org.jboss.logging.annotations.LogMessage;
import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.MessageLogger;
import org.jboss.unimbus.MessageOffsets;
import org.jboss.unimbus.UNimbus;

import static org.jboss.unimbus.UNimbus.PROJECT_CODE;

/**
 * Created by bob on 1/19/18.
 */
@MessageLogger(projectCode = PROJECT_CODE, length = 6)
public interface JDBCMessages extends BasicLogger {
    JDBCMessages MESSAGES = Logger.getMessageLogger(JDBCMessages.class, UNimbus.loggerCategory("jdbc"));

    int OFFSET = MessageOffsets.JDBC_OFFSET;

    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 0 + OFFSET, value = "registered JDBC driver: %s")
    void registeredDriver(String id);
}
