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

package io.thorntail.openapi.impl;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.jboss.jandex.IndexView;

import io.smallrye.openapi.api.OpenApiConfig;
import io.smallrye.openapi.runtime.OpenApiProcessor;

/**
 * Scans a deployment (using the archive and jandex annotation index) for JAX-RS and
 * OpenAPI annotations.  These annotations, if found, are used to generate a valid
 * OpenAPI model.  For reference, see:
 *
 * https://github.com/eclipse/microprofile-open-api/blob/master/spec/src/main/asciidoc/microprofile-openapi-spec.adoc#annotations
 *
 * @author eric.wittmann@gmail.com
 * @author Ken Finnigan
 */
@ApplicationScoped
public class AnnotationModelProducer {
    
    @Inject
    private OpenApiConfig config;
    @Inject
    private IndexView index;

    /**
     * Scan the deployment for relevant annotations.  Produces an OpenAPI data model that was
     * built from those found annotations.
     */
    @Produces
    @OpenApiModel(OpenApiModel.ModelType.ANNOTATIONS)
    public OpenAPI scan() {
        IndexView filteredIndex = new FilteredIndexView(index, config);
        return OpenApiProcessor.modelFromAnnotations(config, filteredIndex);
    }

}
