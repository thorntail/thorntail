package io.thorntail.jwt.auth.impl;

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
public interface JwtAuthMessages extends BasicLogger {
    JwtAuthMessages MESSAGES = Logger.getMessageLogger(JwtAuthMessages.class, LoggingUtil.loggerCategory("jwt-auth"));

    int OFFSET = MessageOffsets.JWT_AUTH_OFFSET;

    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 1 + OFFSET, value = "LoginConfig for deployment %s: method - %s, realm - %s")
    void loginConfig(String depName, String authMethod, String authRealm);

}
