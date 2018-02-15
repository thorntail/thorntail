package org.jboss.unimbus.jca.impl;

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
public interface JCAMessages extends BasicLogger {
    JCAMessages MESSAGES = Logger.getMessageLogger(JCAMessages.class, UNimbus.loggerCategory("jca"));

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
