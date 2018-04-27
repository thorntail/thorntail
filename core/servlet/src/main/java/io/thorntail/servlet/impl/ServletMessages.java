package io.thorntail.servlet.impl;

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
public interface ServletMessages extends BasicLogger {
    ServletMessages MESSAGES = Logger.getMessageLogger(ServletMessages.class, LoggingUtil.loggerCategory("servlet"));

    int OFFSET = MessageOffsets.SERVLET_OFFSET;

    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 1 + OFFSET, value = "%-10s: server started at: %s")
    void serverStarted(String name, String url);

    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 2 + OFFSET, value = "%-10s: deployment %24s: %s")
    void deployment(String type, String name, String contextPath);

}
