package org.jboss.unimbus.datasources;

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
public interface DataSourcesMessages extends BasicLogger {
    DataSourcesMessages MESSAGES = Logger.getMessageLogger(DataSourcesMessages.class, UNimbus.loggerCategory("datasources"));

    int OFFSET = MessageOffsets.DATASOURCES_OFFSET;

    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 0 + OFFSET, value = "datasource for %s bound to %s")
    void dataSourceBound(String url, String jndiName);

    @LogMessage(level = Logger.Level.WARN)
    @Message(id = 1 + OFFSET, value = "unknown configuration parameter %s with value %s")
    void unknownConfigParameter(String key, String value);
}
