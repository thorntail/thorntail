package org.wildfly.swarm.msc;

import org.jboss.logging.BasicLogger;
import org.jboss.logging.Logger;
import org.jboss.logging.annotations.Cause;
import org.jboss.logging.annotations.LogMessage;
import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.MessageLogger;

/**
 * @author Bob McWhirter
 */
@MessageLogger(projectCode = "WFSMSC", length = 4)
public interface MSCMessages extends BasicLogger {
    MSCMessages MESSAGES = Logger.getMessageLogger(MSCMessages.class, "org.wildfly.swarm.msc");

    @LogMessage(level = Logger.Level.ERROR)
    @Message(id = 1, value = "Error reading ServiceActivator asset")
    void errorReadingServiceActivatorAsset(@Cause Throwable cause);
}
