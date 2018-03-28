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
package org.wildfly.swarm.microprofile.openapi.api;

import org.eclipse.microprofile.openapi.OASFilter;
import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.jboss.logging.Logger;
import org.wildfly.swarm.microprofile.openapi.api.models.OpenAPIImpl;
import org.wildfly.swarm.microprofile.openapi.api.models.PathsImpl;
import org.wildfly.swarm.microprofile.openapi.api.models.info.InfoImpl;
import org.wildfly.swarm.microprofile.openapi.api.util.FilterUtil;
import org.wildfly.swarm.microprofile.openapi.api.util.MergeUtil;
import org.wildfly.swarm.microprofile.openapi.api.util.ServersUtil;
import org.wildfly.swarm.microprofile.openapi.runtime.OpenApiConstants;

/**
 * Holds the final OpenAPI document produced during the startup of the app.
 *
 * <p>
 * Note that the model must be initialized first!
 * </p>
 *
 * @author Martin Kouba
 */
public class OpenApiDocument {

    public static final OpenApiDocument INSTANCE = new OpenApiDocument();

    private static final Logger LOGGER = Logger.getLogger(OpenApiDocument.class);

    // These are used during init only
    private transient OpenApiConfig config;
    private transient OpenAPI annotationsModel;
    private transient OpenAPI readerModel;
    private transient OpenAPI staticFileModel;
    private transient OASFilter filter;
    private transient String archiveName;

    private transient OpenAPI model;

    private OpenApiDocument() {
    }

    /**
     *
     * @return the final OpenAPI document produced during the startup of the app
     * @throws IllegalStateException If the final model is not initialized yet
     */
    public OpenAPI get() {
        synchronized (INSTANCE) {
            if (model == null) {
                throw new IllegalStateException("Model not initialized yet");
            }
            return model;
        }
    }

    /**
     * Set the final OpenAPI document. This method should only be used for testing.
     *
     * @param model
     */
    public void set(OpenAPI model) {
        synchronized (INSTANCE) {
            this.model = model;
            LOGGER.info("OpenAPI document set: " + model);
        }
    }

    /**
     * Reset the holder.
     */
    public void reset() {
        synchronized (INSTANCE) {
            model = null;
            clear();
        }
    }

    /**
     * @param {{@code true} if model initialized
     */
    public boolean isSet() {
        synchronized (INSTANCE) {
            return model != null;
        }
    }

    public synchronized void config(OpenApiConfig config) {
        set(() -> this.config = config);
    }

    public void modelFromAnnotations(OpenAPI model) {
        set(() -> this.annotationsModel = model);
    }

    public void modelFromReader(OpenAPI model) {
        set(() -> this.readerModel = model);
    }

    public void modelFromStaticFile(OpenAPI model) {
        set(() -> this.staticFileModel = model);
    }

    public void filter(OASFilter filter) {
        set(() -> this.filter = filter);
    }

    public void archiveName(String archiveName) {
        set(() -> this.archiveName = archiveName);
    }

    public void initialize() {
        synchronized (INSTANCE) {
            if (model != null) {
                modelAlreadyInitialized();
            }
            // Check all the required parts are set
            if (config == null) {
                throw new IllegalStateException("OpenApiConfig must be set before init");
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
            ServersUtil.configureServers(config, merged);

            model = merged;
            LOGGER.info("OpenAPI document initialized: " + model);
            clear();
        }
    }

    /**
     * Filter the final model using a {@link OASFilter} configured by the app. If no filter has been configured, this will simply return the model unchanged.
     *
     * @param model
     */
    private OpenAPI filterModel(OpenAPI model) {
        if (model == null || filter == null) {
            return model;
        }
        LOGGER.info("Filtering OpenAPI model using: " + filter);
        return FilterUtil.applyFilter(filter, model);
    }

    private void set(Runnable action) {
        synchronized (INSTANCE) {
            if (model != null) {
                modelAlreadyInitialized();
            }
            action.run();
        }
    }

    private void modelAlreadyInitialized() {
        throw new IllegalStateException("Model already initialized");
    }

    private void clear() {
        config = null;
        annotationsModel = null;
        readerModel = null;
        staticFileModel = null;
        filter = null;
        archiveName = null;
    }

}
