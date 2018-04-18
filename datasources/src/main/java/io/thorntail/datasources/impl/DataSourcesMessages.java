package io.thorntail.datasources.impl;

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
public interface DataSourcesMessages extends BasicLogger {
    DataSourcesMessages MESSAGES = Logger.getMessageLogger(DataSourcesMessages.class, LoggingUtil.loggerCategory("datasources"));

    int OFFSET = MessageOffsets.DATASOURCES_OFFSET;

    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 0 + OFFSET, value = "datasource for '%s' bound to '%s'")
    void dataSourceBound(String url, String jndiName);

    @LogMessage(level = Logger.Level.WARN)
    @Message(id = 1 + OFFSET, value = "unknown configuration parameter '%s' with value '%s'")
    void unknownConfigParameter(String key, String value);

    @LogMessage(level = Logger.Level.WARN)
    @Message(id = 2 + OFFSET, value = "no registered JDBC drivers")
    void noRegisteredJDBCdrivers();

    @LogMessage(level = Logger.Level.WARN)
    @Message(id = 3 + OFFSET, value = "datasource '%s' specified no JDBC driver, too many to choose from")
    void noDriverSpecifiedManyDrivers(String datasource);

    @LogMessage(level = Logger.Level.WARN)
    @Message(id = 4 + OFFSET, value = "datasource '%s' specified no JDBC driver, using %s")
    void implicitlyUsingDriver(String datasource, String driver);

    @LogMessage(level = Logger.Level.WARN)
    @Message(id = 5 + OFFSET, value = "datasource '%s' specified requested tracing, but tracing is not available")
    void tracingNotAvailable(String id);

    @LogMessage(level = Logger.Level.WARN)
    @Message(id = 6 + OFFSET, value = "datasource '%s' will be traced")
    void tracingEnabled(String id);
}
