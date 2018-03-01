/**
 * Copyright 2018 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wildfly.swarm.microprofile.openapi.runtime;

import java.io.IOException;
import java.io.InputStream;

import javax.inject.Inject;

import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.jboss.logging.Logger;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.Node;
import org.jboss.shrinkwrap.api.classloader.ShrinkWrapClassLoader;
import org.wildfly.swarm.microprofile.openapi.api.OpenApiConfig;
import org.wildfly.swarm.microprofile.openapi.api.OpenApiDocument;
import org.wildfly.swarm.microprofile.openapi.api.models.OpenAPIImpl;
import org.wildfly.swarm.microprofile.openapi.runtime.io.OpenApiParser;
import org.wildfly.swarm.microprofile.openapi.runtime.io.OpenApiSerializer.Format;
import org.wildfly.swarm.spi.api.DeploymentProcessor;
import org.wildfly.swarm.spi.runtime.annotations.DeploymentScoped;
import org.wildfly.swarm.undertow.WARArchive;

/**
 * @author eric.wittmann@gmail.com
 */
@SuppressWarnings("rawtypes")
@DeploymentScoped
public class OpenApiDeploymentProcessor implements DeploymentProcessor {

    private static final Logger LOGGER = Logger.getLogger(OpenApiDeploymentProcessor.class);

    private static final String LISTENER_CLASS = "org.wildfly.swarm.microprofile.openapi.deployment.OpenApiServletContextListener";

    private final OpenApiConfig config;

    private final Archive archive;

    /**
     * Constructor for testing purposes.
     *
     * @param config
     * @param archive
     */
    public OpenApiDeploymentProcessor(OpenApiConfig config, Archive archive) {
        this.config = config;
        this.archive = archive;
    }

    /**
     * Constructor.
     *
     * @param archive
     */
    @Inject
    public OpenApiDeploymentProcessor(Archive archive) {
        this.config = initConfigFromArchive(archive);
        this.archive = archive;
    }

    /**
     * Process the deployment in order to produce an OpenAPI document.
     *
     * @see org.wildfly.swarm.spi.api.DeploymentProcessor#process()
     */
    @Override
    public void process() throws Exception {
        try {
            // First register OpenApiServletContextListener which triggers the final init
            WARArchive warArchive = archive.as(WARArchive.class);
            warArchive.findWebXmlAsset().addListener(LISTENER_CLASS);
        } catch (Exception e) {
            throw new RuntimeException("Failed to register OpenAPI listener", e);
        }
        // Set models from annotations and static file
        OpenApiDocument openApiDocument = OpenApiDocument.INSTANCE;
        openApiDocument.config(config);
        openApiDocument.modelFromStaticFile(modelFromStaticFile());
        openApiDocument.modelFromAnnotations(modelFromAnnotations());
    }

    /**
     * Find a static file located in the deployment and, if it exists, parse it and
     * return the resulting model.  If no static file is found, returns null.  If an
     * error is encountered while parsing the file then a runtime exception is
     * thrown.
     */
    private OpenAPIImpl modelFromStaticFile() {
        Format format = Format.YAML;

        // Check for the file in both META-INF and WEB-INF/classes/META-INF
        Node node = archive.get("/META-INF/openapi.yaml");
        if (node == null) {
            node = archive.get("/WEB-INF/classes/META-INF/openapi.yml");
        }
        if (node == null) {
            node = archive.get("/META-INF/openapi.yml");
        }
        if (node == null) {
            node = archive.get("/WEB-INF/classes/META-INF/openapi.yml");
        }
        if (node == null) {
            node = archive.get("/META-INF/openapi.json");
            format = Format.JSON;
        }
        if (node == null) {
            node = archive.get("/WEB-INF/classes/META-INF/openapi.json");
            format = Format.JSON;
        }

        if (node == null) {
            return null;
        }

        try (InputStream stream = node.getAsset().openStream()) {
            return OpenApiParser.parse(stream, format);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Create an {@link OpenAPI} model by scanning the deployment for relevant JAX-RS and
     * OpenAPI annotations.  If scanning is disabled, this method returns null.  If scanning
     * is enabled but no relevant annotations are found, an empty OpenAPI model is returned.
     */
    private OpenAPIImpl modelFromAnnotations() {
        if (this.config.scanDisable()) {
            return null;
        }

        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(config, archive);
        return scanner.scan();
    }

    private static OpenApiConfig initConfigFromArchive(Archive<?> archive) {
        ShrinkWrapClassLoader cl = new ShrinkWrapClassLoader(archive);
        try {
            return new OpenApiConfig(ConfigProvider.getConfig(cl));
        } finally {
            try {
                cl.close();
            } catch (IOException e) {
                LOGGER.warnv("Could not close ShrinkWrapClassLoader for {0}", archive.getName());
            }
        }
    }

}
