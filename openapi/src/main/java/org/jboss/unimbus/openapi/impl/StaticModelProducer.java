package org.jboss.unimbus.openapi.impl;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.jboss.unimbus.UNimbus;
import org.jboss.unimbus.openapi.impl.io.OpenApiParser;
import org.jboss.unimbus.openapi.impl.io.OpenApiSerializer.Format;

/**
 * @author Ken Finnigan
 */
@ApplicationScoped
public class StaticModelProducer {
    @Inject
    private UNimbus uNimbus;

    @Produces
    @OpenApiModel(OpenApiModel.ModelType.STATIC)
    public OpenAPI staticModel() {
        Format format = Format.YAML;

        // Check for the file in both META-INF and WEB-INF/classes/META-INF
        URL resourceUrl = uNimbus.getClassLoader().getResource("META-INF/openapi.yaml");
        if (resourceUrl == null) {
            resourceUrl = uNimbus.getClassLoader().getResource("WEB-INF/classes/META-INF/openapi.yaml");
        }
        if (resourceUrl == null) {
            resourceUrl = uNimbus.getClassLoader().getResource("META-INF/openapi.yml");
        }
        if (resourceUrl == null) {
            resourceUrl = uNimbus.getClassLoader().getResource("WEB-INF/classes/META-INF/openapi.yml");
        }
        if (resourceUrl == null) {
            resourceUrl = uNimbus.getClassLoader().getResource("META-INF/openapi.json");
            format = Format.JSON;
        }
        if (resourceUrl == null) {
            resourceUrl = uNimbus.getClassLoader().getResource("WEB-INF/classes/META-INF/openapi.json");
            format = Format.JSON;
        }

        if (resourceUrl == null) {
            return null;
        }

        try (InputStream stream = resourceUrl.openStream()) {
            return OpenApiParser.parse(stream, format);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
