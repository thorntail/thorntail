package io.thorntail.openapi.impl;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import org.eclipse.microprofile.openapi.models.OpenAPI;

import io.smallrye.openapi.runtime.OpenApiProcessor;
import io.smallrye.openapi.runtime.OpenApiStaticFile;
import io.smallrye.openapi.runtime.io.OpenApiSerializer.Format;
import io.thorntail.Thorntail;

/**
 * @author Ken Finnigan
 */
@ApplicationScoped
public class StaticModelProducer {

    @Inject
    private Thorntail thorntail;

    @Produces
    @OpenApiModel(OpenApiModel.ModelType.STATIC)
    public OpenAPI staticModel() {

        Format format = Format.YAML;

        // Check for the file in both META-INF and WEB-INF/classes/META-INF
        URL resourceUrl = thorntail.getClassLoader().getResource("META-INF/openapi.yaml");
        if (resourceUrl == null) {
            resourceUrl = thorntail.getClassLoader().getResource("WEB-INF/classes/META-INF/openapi.yaml");
        }
        if (resourceUrl == null) {
            resourceUrl = thorntail.getClassLoader().getResource("META-INF/openapi.yml");
        }
        if (resourceUrl == null) {
            resourceUrl = thorntail.getClassLoader().getResource("WEB-INF/classes/META-INF/openapi.yml");
        }
        if (resourceUrl == null) {
            resourceUrl = thorntail.getClassLoader().getResource("META-INF/openapi.json");
            format = Format.JSON;
        }
        if (resourceUrl == null) {
            resourceUrl = thorntail.getClassLoader().getResource("WEB-INF/classes/META-INF/openapi.json");
            format = Format.JSON;
        }

        if (resourceUrl == null) {
            return null;
        }
        
        InputStream is = null;
        try {
            is = resourceUrl.openStream();
            try (OpenApiStaticFile staticFile = new OpenApiStaticFile(is, format)) {
                return OpenApiProcessor.modelFromStaticFile(staticFile);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
