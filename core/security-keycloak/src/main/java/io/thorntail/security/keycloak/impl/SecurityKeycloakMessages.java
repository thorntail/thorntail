package io.thorntail.security.keycloak.impl;

import org.jboss.logging.BasicLogger;
import org.jboss.logging.Logger;
import org.jboss.logging.annotations.LogMessage;
import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.MessageLogger;

import io.thorntail.logging.impl.LoggingUtil;
import io.thorntail.logging.impl.MessageOffsets;

/**
 * Created by bob on 1/19/18.
 */
@MessageLogger(projectCode = LoggingUtil.CODE, length = 6)
public interface SecurityKeycloakMessages extends BasicLogger {
    SecurityKeycloakMessages MESSAGES = Logger.getMessageLogger(SecurityKeycloakMessages.class, LoggingUtil.loggerCategory("security.keycloak"));

    int OFFSET = MessageOffsets.KEYCLOAK_OFFSET;

    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 1 + OFFSET, value = "Adding KeycloakConfigResolver to the deployment %s")
    void configResolverForDeployment(String name);
    
    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 2 + OFFSET, value = "Deployment %s has no 'KEYCLOAK' authentication method enabled")
    void noKeycloakForDeployment(String name);

}
