/*
 * Copyright 2018 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.thorntail.openapi.impl;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import org.eclipse.microprofile.openapi.OASFilter;
import org.eclipse.microprofile.openapi.models.OpenAPI;

import io.smallrye.openapi.api.OpenApiConfig;
import io.smallrye.openapi.api.OpenApiDocument;
import io.smallrye.openapi.runtime.OpenApiProcessor;
import io.thorntail.Thorntail;
import io.thorntail.events.LifecycleEvent;

/**
 * @author eric.wittmann@gmail.com
 */
@ApplicationScoped
public class OpenApiDocumentProducer {
    
    @Inject
    private OpenApiConfig config;
    @Inject
    private Thorntail thorntail;
    
    private transient OpenApiDocument document;
    
    @PostConstruct
    public void init() {
        document = OpenApiDocument.INSTANCE;
        document.reset();
        document.config(config);
    }
    
    /**
     * Initialize the document.
     * @param event
     * @param annotationsModel
     * @param readerModel
     * @param staticFileModel
     */
    public void initialize(@Observes LifecycleEvent.Initialize event,
            @OpenApiModel(OpenApiModel.ModelType.ANNOTATIONS) OpenAPI annotationsModel,
            @OpenApiModel(OpenApiModel.ModelType.READER) OpenAPI readerModel,
            @OpenApiModel(OpenApiModel.ModelType.STATIC) OpenAPI staticFileModel) {
        document.modelFromAnnotations(annotationsModel);
        document.modelFromReader(readerModel);
        document.modelFromStaticFile(staticFileModel);
        document.filter(filter());
        document.initialize();
    }

    @Produces
    public OpenApiDocument openApiDocument() {
        return document;
    }

    private OASFilter filter() {
        return OpenApiProcessor.getFilter(config, thorntail.getClassLoader());
    }
    
}
