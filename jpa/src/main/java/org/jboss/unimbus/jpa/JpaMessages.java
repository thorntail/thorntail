package org.jboss.unimbus.jpa;

import java.lang.annotation.Annotation;
import java.lang.reflect.Member;

import org.jboss.logging.BasicLogger;
import org.jboss.logging.Logger;
import org.jboss.logging.annotations.LogMessage;
import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.MessageLogger;
import org.jboss.unimbus.UNimbus;

import static org.jboss.unimbus.MessageOffsets.JPA_OFFSET;
import static org.jboss.unimbus.UNimbus.PROJECT_CODE;

/**
 * @author Ken Finnigan
 */
@MessageLogger(projectCode = PROJECT_CODE, length = 6)
public interface JpaMessages extends BasicLogger {
    JpaMessages MESSAGES = Logger.getMessageLogger(JpaMessages.class, UNimbus.loggerCategory("jpa"));

    @Message(id = 0 + JPA_OFFSET, value = "%s annotation not found on %s")
    IllegalArgumentException annotationNotFound(Class<? extends Annotation> type, Member member);

    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 1 + JPA_OFFSET, value = "Creating %s for '%s'")
    void createFactoryForPersistence(Class<? extends Annotation> type, String name);
}
