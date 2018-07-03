package io.thorntail.openapi.impl;

import static io.thorntail.logging.impl.LoggingUtil.CODE;

import java.io.IOException;

import org.jboss.logging.BasicLogger;
import org.jboss.logging.Logger;
import org.jboss.logging.annotations.Cause;
import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.MessageLogger;

import io.smallrye.openapi.runtime.io.OpenApiSerializer;
import io.thorntail.logging.impl.LoggingUtil;
import io.thorntail.logging.impl.MessageOffsets;

/**
 * Created by bob on 1/19/18.
 */
@MessageLogger(projectCode = CODE, length = 6)
public interface OpenApiMessages extends BasicLogger {
    OpenApiMessages MESSAGES = Logger.getMessageLogger(OpenApiMessages.class, LoggingUtil.loggerCategory("openapi"));

    int OFFSET = MessageOffsets.OPENAPI_OFFSET;

    @Message(id = 0 + OFFSET, value = "Unable to serialize OpenAPI in %s")
    RuntimeException unableToSerializeOpenApiModel(OpenApiSerializer.Format format, @Cause IOException ioe);

}
