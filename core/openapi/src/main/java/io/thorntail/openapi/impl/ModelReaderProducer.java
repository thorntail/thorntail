package io.thorntail.openapi.impl;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import org.eclipse.microprofile.openapi.models.OpenAPI;

import io.smallrye.openapi.api.OpenApiConfig;
import io.smallrye.openapi.runtime.OpenApiProcessor;
import io.thorntail.Thorntail;

/**
 * @author Ken Finnigan
 */
@ApplicationScoped
public class ModelReaderProducer {

    @Inject
    private OpenApiConfig config;
    @Inject
    private Thorntail thorntail;

    @Produces
    @OpenApiModel(OpenApiModel.ModelType.READER)
    public OpenAPI modelReader() {
        return OpenApiProcessor.modelFromReader(config, thorntail.getClassLoader());
    }
}
