/*
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
package io.thorntail.openapi.impl.api;

import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import io.thorntail.Thorntail;
import io.thorntail.openapi.impl.api.models.PathsImpl;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.openapi.OASConfig;
import org.eclipse.microprofile.openapi.OASFilter;
import org.eclipse.microprofile.openapi.models.OpenAPI;
import io.thorntail.events.LifecycleEvent;
import io.thorntail.openapi.impl.OpenApiConstants;
import io.thorntail.openapi.impl.OpenApiMessages;
import io.thorntail.openapi.impl.OpenApiModel;
import io.thorntail.openapi.impl.api.models.OpenAPIImpl;
import io.thorntail.openapi.impl.api.models.info.InfoImpl;
import io.thorntail.openapi.impl.api.util.FilterUtil;
import io.thorntail.openapi.impl.api.util.MergeUtil;
import io.thorntail.openapi.impl.api.util.ServersUtil;

/**
 * Holds the final OpenAPI document produced during the startup of the app.
 *
 * <p>
 * Note that the model must be initialized first!
 * </p>
 *
 * @author Martin Kouba
 * @author Ken Finnigan
 */
@ApplicationScoped
public class OpenApiDocument {

    @Inject
    @ConfigProperty(name = OASConfig.FILTER)
    private Optional<String> filterClassName;

    @Inject
    private Thorntail thorntail;

    @Inject
    private ServersUtil serversUtil;

    private transient OASFilter filter;

    private transient String archiveName;

    private transient OpenAPI model;

    /**
     * @return the final OpenAPI document produced during the startup of the app
     * @throws IllegalStateException If the final model is not initialized yet
     */
    public OpenAPI get() {
        if (model == null) {
            throw new IllegalStateException("Model not initialized yet");
        }
        return model;
    }

    /**
     * Reset the holder.
     */
    public void reset() {
        model = null;
        clear();
    }

    public void initialize(@Observes LifecycleEvent.Initialize event,
                           @OpenApiModel(OpenApiModel.ModelType.ANNOTATIONS) OpenAPI annotationsModel,
                           @OpenApiModel(OpenApiModel.ModelType.READER) OpenAPI readerModel,
                           @OpenApiModel(OpenApiModel.ModelType.STATIC) OpenAPI staticFileModel) {
        if (model != null) {
            throw OpenApiMessages.MESSAGES.modelAlreadyInitialized();
        }

        // Phase 1: Use OASModelReader
        OpenAPI merged = readerModel;

        // Phase 2: Merge any static OpenAPI file packaged in the app
        merged = MergeUtil.mergeObjects(merged, staticFileModel);

        // Phase 3: Merge annotations
        merged = MergeUtil.mergeObjects(merged, annotationsModel);

        // Phase 4: Filter model via OASFilter
        merged = filterModel(merged);

        // Phase 5: Default empty document if model == null
        if (merged == null) {
            merged = new OpenAPIImpl();
            merged.setOpenapi(OpenApiConstants.OPEN_API_VERSION);
        }

        // Phase 6: Provide missing required elements
        if (merged.getPaths() == null) {
            merged.setPaths(new PathsImpl());
        }
        if (merged.getInfo() == null) {
            merged.setInfo(new InfoImpl());
        }
        if (merged.getInfo().getTitle() == null) {
            merged.getInfo().setTitle((archiveName == null ? "Generated" : archiveName) + " API");
        }
        if (merged.getInfo().getVersion() == null) {
            merged.getInfo().setVersion("1.0");
        }

        // Phase 7: Use Config values to add Servers (global, pathItem, operation)
        this.serversUtil.configureServers(merged);

        model = merged;
        OpenApiMessages.MESSAGES.openApiModelInitialized(model);
        clear();
    }

    /**
     * Filter the final model using a {@link OASFilter} configured by the app. If no filter has been configured, this will simply return the model unchanged.
     *
     * @param model
     */
    private OpenAPI filterModel(OpenAPI model) {
        if (model == null || !filterClassName.isPresent()) {
            return model;
        }

        try {
            Class<?> c = thorntail.getClassLoader().loadClass(filterClassName.get());
            this.filter = (OASFilter) c.newInstance();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        OpenApiMessages.MESSAGES.filteringOpenApiModel(filter);
        return FilterUtil.applyFilter(filter, model);
    }

    private void clear() {
        filter = null;
        archiveName = null;
    }

}
