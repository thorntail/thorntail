package org.jboss.unimbus.metrics;

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
public interface MetricsMessages extends BasicLogger {
    MetricsMessages MESSAGES = Logger.getMessageLogger(MetricsMessages.class, "org.jboss.unimbus");

    int OFFSET = 300;

    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 0 + OFFSET, value = "registered metric: %s:%s")
    void registeredMetric(String scope, String metricName);
}
