package io.thorntail.vertx.web;

import static io.thorntail.logging.impl.LoggingUtil.CODE;
import static org.jboss.logging.Logger.Level.DEBUG;
import static org.jboss.logging.Logger.Level.ERROR;
import static org.jboss.logging.Logger.Level.INFO;
import static org.jboss.logging.Logger.Level.TRACE;
import static org.jboss.logging.Logger.Level.WARN;

import org.jboss.logging.BasicLogger;
import org.jboss.logging.Logger;
import org.jboss.logging.annotations.Cause;
import org.jboss.logging.annotations.LogMessage;
import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.MessageLogger;

import io.thorntail.logging.impl.LoggingUtil;
import io.thorntail.vertx.VertxLogger;

/**
 * @author Martin Kouba
 */
@MessageLogger(projectCode = CODE, length = 6)
public interface VertxWebLogger extends BasicLogger {

    VertxWebLogger LOG = Logger.getMessageLogger(VertxWebLogger.class, LoggingUtil.loggerCategory("vertx"));

    int OFFSET = VertxLogger.OFFSET + 100;

    @LogMessage(level = DEBUG)
    @Message(id = 1 + OFFSET, value = "Route handler found: %s")
    void routeHandlerFound(Object annotatedType);

    @LogMessage(level = DEBUG)
    @Message(id = 2 + OFFSET, value = "Route observer found: %s")
    void routeObserverFound(Object method);

    @LogMessage(level = WARN)
    @Message(id = 3 + OFFSET, value = "Ignoring non-observer method annotated with @WebRoute: %s")
    void ignoringNonObserverMethod(Object method);

    @LogMessage(level = TRACE)
    @Message(id = 4 + OFFSET, value = "Add Id qualifier %s to parameter %s")
    void addIdQualifier(Object id, Object annotated);

    @LogMessage(level = DEBUG)
    @Message(id = 5 + OFFSET, value = "Route registered for %s")
    void routeRegistered(Object webRoute);

    @LogMessage(level = WARN)
    @Message(id = 6 + OFFSET, value = "Class annotated with @WebRoute must be top-level or static nested class - ignoring %s")
    void classNotTopLevelOrStaticNested(Object clazz);

    @LogMessage(level = WARN)
    @Message(id = 7 + OFFSET, value = "Class annotated with @WebRoute must implement io.vertx.core.Handler<RoutingContext> - ignoring %s")
    void classNotHandler(Object clazz);

    @LogMessage(level = ERROR)
    @Message(id = 8 + OFFSET, value = "Error disposing a route handler: %s")
    void errorDisposingHandler(Object annotatedType, @Cause Throwable cause);

    @LogMessage(level = DEBUG)
    @Message(id = 9 + OFFSET, value = "HttpServerOptions used: \n%s")
    void usingOptions(Object options);

    @LogMessage(level = INFO)
    @Message(id = 10 + OFFSET, value = "HttpServer listening on %s")
    void httpServerListening(int port);

}
