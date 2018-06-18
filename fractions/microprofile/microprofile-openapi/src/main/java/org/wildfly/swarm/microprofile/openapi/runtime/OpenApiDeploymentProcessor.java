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

import javax.inject.Inject;

import org.eclipse.microprofile.config.ConfigProvider;
import org.jboss.logging.Logger;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.classloader.ShrinkWrapClassLoader;
import org.wildfly.swarm.spi.api.DeploymentProcessor;
import org.wildfly.swarm.spi.runtime.annotations.DeploymentScoped;
import org.wildfly.swarm.undertow.WARArchive;

import io.smallrye.openapi.api.OpenApiConfig;
import io.smallrye.openapi.api.OpenApiDocument;
import io.smallrye.openapi.runtime.OpenApiProcessor;

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
        openApiDocument.modelFromStaticFile(OpenApiProcessor.modelFromStaticFile(config, this.archive));
        openApiDocument.modelFromAnnotations(OpenApiProcessor.modelFromAnnotations(config, this.archive));
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
