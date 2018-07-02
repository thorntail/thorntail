package io.thorntail.vertx;

import static io.thorntail.logging.impl.LoggingUtil.CODE;
import static org.jboss.logging.Logger.Level.DEBUG;
import static org.jboss.logging.Logger.Level.ERROR;
import static org.jboss.logging.Logger.Level.INFO;
import static org.jboss.logging.Logger.Level.WARN;

import org.jboss.logging.BasicLogger;
import org.jboss.logging.Logger;
import org.jboss.logging.annotations.Cause;
import org.jboss.logging.annotations.LogMessage;
import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.MessageLogger;

import io.thorntail.logging.impl.LoggingUtil;
import io.thorntail.logging.impl.MessageOffsets;
import io.vertx.core.VertxOptions;

/**
 * @author Martin Kouba
 */
@MessageLogger(projectCode = CODE, length = 6)
public interface VertxLogger extends BasicLogger {

    VertxLogger LOG = Logger.getMessageLogger(VertxLogger.class, LoggingUtil.loggerCategory("vertx"));

    int OFFSET = MessageOffsets.VERTX_OFFSET + 100;

    @LogMessage(level = INFO)
    @Message(id = 1 + OFFSET, value = "Vertx instance created")
    void vertxInstanceCreated();

    @LogMessage(level = INFO)
    @Message(id = 2 + OFFSET, value = "Clustered Vertx instance created")
    void clusteredVertxInstanceCreated();

    @LogMessage(level = DEBUG)
    @Message(id = 3 + OFFSET, value = "VertxOptions used: \n%s")
    void usingOptions(VertxOptions options);

    @LogMessage(level = WARN)
    @Message(id = 4 + OFFSET, value = "VertxMessage observer found but @VertxConsume not declared: %s")
    void vertxMessageObserverWithoutConsumeFound(Object observer);

    @LogMessage(level = DEBUG)
    @Message(id = 5 + OFFSET, value = "VertxMessage observer found: %s")
    void vertxMessageObserverFound(Object observer);

    @LogMessage(level = DEBUG)
    @Message(id = 6 + OFFSET, value = "Sucessfully registered event consumer for address: %s")
    void registerConsumerOk(Object address);

    @LogMessage(level = ERROR)
    @Message(id = 7 + OFFSET, value = "Could not register event consumer for address: %s")
    void registerConsumerError(Object address, @Cause Throwable cause);

    @LogMessage(level = DEBUG)
    @Message(id = 8 + OFFSET, value = "Deploying Verticle: %s")
    void deployVerticle(Object verticleClazz);

    @LogMessage(level = DEBUG)
    @Message(id = 9 + OFFSET, value = "Add observer for VertxPublish event: %s")
    void addPublishObserver(Object injectionPoint);

    @LogMessage(level = DEBUG)
    @Message(id = 10 + OFFSET, value = "Add observer for VertxSend event: %s")
    void addSendObserver(Object injectionPoint);

    @LogMessage(level = INFO)
    @Message(id = 11 + OFFSET, value = "A reply was already sent to %s - reply %s is ignored")
    void replyAlreadySent(Object replyAddress, Object reply);

    @LogMessage(level = WARN)
    @Message(id = 12 + OFFSET, value = "Message sent to %s has no reply handler - reply %s is ignored")
    void noReplyHandlerSet(Object address, Object reply);

    @LogMessage(level = DEBUG)
    @Message(id = 13 + OFFSET, value = "Failure during observer notification")
    void observerNotificationFailure(@Cause Throwable cause);

    @LogMessage(level = DEBUG)
    @Message(id = 14 + OFFSET, value = "Starting span for VertxMessage observer: %s")
    void startSpanForVertxMessageObserver(Object observer);
}
