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

import javax.inject.Inject;

import org.jboss.jandex.IndexView;
import org.jboss.shrinkwrap.api.Archive;
import org.wildfly.swarm.container.runtime.cdi.DeploymentContext;
import org.wildfly.swarm.spi.api.DeploymentProcessor;
import org.wildfly.swarm.spi.runtime.annotations.DeploymentScoped;
import org.wildfly.swarm.undertow.WARArchive;

import io.smallrye.openapi.api.OpenApiConfig;
import io.smallrye.openapi.api.OpenApiDocument;
import io.smallrye.openapi.api.util.ArchiveUtil;
import io.smallrye.openapi.runtime.OpenApiProcessor;
import io.smallrye.openapi.runtime.OpenApiStaticFile;

/**
 * @author eric.wittmann@gmail.com
 */
@SuppressWarnings("rawtypes")
@DeploymentScoped
public class OpenApiDeploymentProcessor implements DeploymentProcessor {

    private static final String LISTENER_CLASS = "org.wildfly.swarm.microprofile.openapi.deployment.OpenApiServletContextListener";

    private final OpenApiConfig config;

    private final Archive archive;

    private final IndexView index;

    @Inject
    DeploymentContext deploymentContext;

    /**
     * Constructor for testing purposes.
     *
     * @param config
     * @param archive
     */
    public OpenApiDeploymentProcessor(OpenApiConfig config, Archive archive) {
        this.config = config;
        this.archive = archive;
        this.index = ArchiveUtil.archiveToIndex(config, archive);
    }

    /**
     * Constructor.
     *
     * @param archive
     */
    @Inject
    public OpenApiDeploymentProcessor(Archive archive) {
        this.config = ArchiveUtil.archiveToConfig(archive);
        this.archive = archive;
        this.index = ArchiveUtil.archiveToIndex(config, archive);
    }

    /**
     * Process the deployment in order to produce an OpenAPI document.
     *
     * @see org.wildfly.swarm.spi.api.DeploymentProcessor#process()
     */
    @Override
    public void process() throws Exception {
        // if the deployment is Implicit, we don't want to process it
        if (deploymentContext != null && deploymentContext.isImplicit()) {
            return;
        }
        try {
            // First register OpenApiServletContextListener which triggers the final init
            WARArchive warArchive = archive.as(WARArchive.class);
            warArchive.findWebXmlAsset().addListener(LISTENER_CLASS);
        } catch (Exception e) {
            throw new RuntimeException("Failed to register OpenAPI listener", e);
        }

        OpenApiStaticFile staticFile = ArchiveUtil.archiveToStaticFile(archive);

        // Set models from annotations and static file
        OpenApiDocument openApiDocument = OpenApiDocument.INSTANCE;
        openApiDocument.config(config);
        openApiDocument.modelFromStaticFile(OpenApiProcessor.modelFromStaticFile(staticFile));
        openApiDocument.modelFromAnnotations(OpenApiProcessor.modelFromAnnotations(config, index));
    }

}
