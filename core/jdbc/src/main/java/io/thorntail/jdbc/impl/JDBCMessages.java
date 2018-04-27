package io.thorntail.jdbc.impl;

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
public interface JDBCMessages extends BasicLogger {
    JDBCMessages MESSAGES = Logger.getMessageLogger(JDBCMessages.class, LoggingUtil.loggerCategory("jdbc"));

    int OFFSET = MessageOffsets.JDBC_OFFSET;

    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 0 + OFFSET, value = "registered JDBC driver: %s")
    void registeredDriver(String id);
}
