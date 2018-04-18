package io.thorntail.jpa.impl;

import java.lang.annotation.Annotation;
import java.lang.reflect.Member;

import org.jboss.logging.BasicLogger;
import org.jboss.logging.Logger;
import org.jboss.logging.annotations.LogMessage;
import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.MessageLogger;
import io.thorntail.logging.impl.LoggingUtil;

import static io.thorntail.logging.impl.MessageOffsets.JPA_OFFSET;
import static io.thorntail.logging.impl.LoggingUtil.CODE;

/**
 * @author Ken Finnigan
 */
@MessageLogger(projectCode = CODE, length = 6)
public interface JpaMessages extends BasicLogger {
    JpaMessages MESSAGES = Logger.getMessageLogger(JpaMessages.class, LoggingUtil.loggerCategory("jpa"));

    @Message(id = 0 + JPA_OFFSET, value = "%s annotation not found on %s")
    IllegalArgumentException annotationNotFound(Class<? extends Annotation> type, Member member);

    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 1 + JPA_OFFSET, value = "creating %s for '%s'")
    void createFactoryForPersistence(Class<? extends Annotation> type, String name);

    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 10 + JPA_OFFSET, value = "tracing enabled for '%s'")
    void tracingEnabled(String puName);

    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 11 + JPA_OFFSET, value = "initializing persistence contexts")
    void persistencecontextInitialization();
}
