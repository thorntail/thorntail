package io.thorntail.jca.impl;

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
public interface JCAMessages extends BasicLogger {
    JCAMessages MESSAGES = Logger.getMessageLogger(JCAMessages.class, LoggingUtil.loggerCategory("jca"));

    int OFFSET = MessageOffsets.JCA_OFFSET;

    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 0 + OFFSET, value = "registered resource-adapter deployment: %s")
    void registeredDeployment(String ra);

    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 1 + OFFSET, value = "deployed resource-adapter: %s")
    void deployedResourceAdapter(String ra);

    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 2 + OFFSET, value = "deployed message-driven: %s")
    void deployedMessageDriven(String cls);

    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 3 + OFFSET, value = "bound %s: %s")
    void bound(String objectName, String jndiName);


}
