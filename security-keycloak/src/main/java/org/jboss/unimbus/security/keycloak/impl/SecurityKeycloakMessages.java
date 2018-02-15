package org.jboss.unimbus.security.keycloak.impl;

import org.jboss.logging.BasicLogger;
import org.jboss.logging.Logger;
import org.jboss.logging.annotations.MessageLogger;
import org.jboss.unimbus.logging.impl.LoggingUtil;
import org.jboss.unimbus.logging.impl.MessageOffsets;

/**
 * Created by bob on 1/19/18.
 */
@MessageLogger(projectCode = LoggingUtil.CODE, length = 6)
public interface SecurityKeycloakMessages extends BasicLogger {
    SecurityKeycloakMessages MESSAGES = Logger.getMessageLogger(SecurityKeycloakMessages.class, LoggingUtil.loggerCategory("security.keycloak"));

    int OFFSET = MessageOffsets.KEYCLOAK_OFFSET;

    /*
    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 1 + OFFSET, value = "Temporary 'admin' password: %s")
    void temporaryAdminPassword(String password);
    */

}
