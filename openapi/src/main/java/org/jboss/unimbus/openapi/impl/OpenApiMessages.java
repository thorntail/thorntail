package org.jboss.unimbus.openapi.impl;

import java.io.IOException;

import org.eclipse.microprofile.openapi.OASFilter;
import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.jboss.logging.BasicLogger;
import org.jboss.logging.Logger;
import org.jboss.logging.annotations.Cause;
import org.jboss.logging.annotations.LogMessage;
import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.MessageLogger;
import org.jboss.unimbus.logging.impl.LoggingUtil;
import org.jboss.unimbus.logging.impl.MessageOffsets;
import org.jboss.unimbus.openapi.impl.io.OpenApiSerializer;

import static org.jboss.unimbus.logging.impl.LoggingUtil.CODE;

/**
 * Created by bob on 1/19/18.
 */
@MessageLogger(projectCode = CODE, length = 6)
public interface OpenApiMessages extends BasicLogger {
    OpenApiMessages MESSAGES = Logger.getMessageLogger(OpenApiMessages.class, LoggingUtil.loggerCategory("openapi"));

    int OFFSET = MessageOffsets.OPENAPI_OFFSET;

    @LogMessage(level = Logger.Level.DEBUG)
    @Message(id = 0 + OFFSET, value = "Scanning deployment for OpenAPI and JAX-RS Annotations")
    void scanningDeployment();

    @LogMessage(level = Logger.Level.DEBUG)
    @Message(id = 1 + OFFSET, value = "Processing JAX-RS resource class: %s")
    void processJaxrsResource(String resourceName);

    @LogMessage(level = Logger.Level.DEBUG)
    @Message(id = 2 + OFFSET, value = "Processing JAX-RS method: %s")
    void processJaxrsMethod(String methodName);

    @LogMessage(level = Logger.Level.DEBUG)
    @Message(id = 3 + OFFSET, value = "Processing %s annotation")
    void processingAnnotation(String annotation);

    @LogMessage(level = Logger.Level.DEBUG)
    @Message(id = 4 + OFFSET, value = "Processing array of %s annotations")
    void processingArrayOfAnnotation(String annotation);

    @LogMessage(level = Logger.Level.ERROR)
    @Message(id = 5 + OFFSET, value = "Error reading a %s annotation")
    void errorReadingAnnotation(String annotation, @Cause Exception e);

    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 6 + OFFSET, value = "Scanning is disabled, deployment will not be scanned for annotations")
    void annotationScanningDisabled();

    @Message(id = 7 + OFFSET, value = "Model already initialized")
    IllegalStateException modelAlreadyInitialized();

    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 8 + OFFSET, value = "OpenAPI document initialized: %s")
    void openApiModelInitialized(OpenAPI model);

    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 9 + OFFSET, value = "Filtering OpenAPI model using: %s")
    void filteringOpenApiModel(OASFilter filter);

    @Message(id = 10 + OFFSET, value = "Unable to serialize OpenAPI in %s")
    RuntimeException unableToSerializeOpenApiModel(OpenApiSerializer.Format format, @Cause IOException ioe);

}
