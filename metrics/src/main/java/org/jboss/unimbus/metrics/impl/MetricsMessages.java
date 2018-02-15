package org.jboss.unimbus.metrics.impl;

import org.jboss.logging.BasicLogger;
import org.jboss.logging.Logger;
import org.jboss.logging.annotations.LogMessage;
import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.MessageLogger;
import org.jboss.unimbus.logging.impl.MessageOffsets;
import org.jboss.unimbus.UNimbus;

import static org.jboss.unimbus.UNimbus.PROJECT_CODE;

/**
 * Created by bob on 1/19/18.
 */
@MessageLogger(projectCode = PROJECT_CODE, length = 6)
public interface MetricsMessages extends BasicLogger {
    MetricsMessages MESSAGES = Logger.getMessageLogger(MetricsMessages.class, UNimbus.loggerCategory("metrics"));

    int OFFSET = MessageOffsets.METRICS_OFFSET;

    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 0 + OFFSET, value = "registered metric: %s:%s")
    void registeredMetric(String scope, String metricName);
}
