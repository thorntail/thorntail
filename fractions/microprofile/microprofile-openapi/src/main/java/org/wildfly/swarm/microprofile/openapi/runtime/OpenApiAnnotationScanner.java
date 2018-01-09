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

import org.jboss.jandex.IndexView;
import org.jboss.shrinkwrap.api.Archive;
import org.wildfly.swarm.microprofile.openapi.models.OpenAPIImpl;

/**
 * Scans a deployment (using the archive and jandex annotation index) for JAX-RS and
 * OpenAPI annotations.  These annotations, if found, are used to generate a valid
 * OpenAPI model.  For reference, see:
 *
 * https://github.com/eclipse/microprofile-open-api/blob/master/spec/src/main/asciidoc/microprofile-openapi-spec.adoc#annotations
 *
 * @author eric.wittmann@gmail.com
 */
@SuppressWarnings("rawtypes")
public class OpenApiAnnotationScanner {

    private static final String OPEN_API_VERSION = "3.0.1";

    private final OpenApiConfig config;
    private final Archive archive;
    private final IndexView index;

    /**
     * Constructor.
     * @param config
     * @param archive
     * @param index
     */
    public OpenApiAnnotationScanner(OpenApiConfig config, Archive archive, IndexView index) {
        this.config = config;
        this.archive = archive;
        this.index = index;
    }

    public OpenAPIImpl scan() {
        OpenAPIImpl openApi = new OpenAPIImpl();
        openApi.setOpenapi(OPEN_API_VERSION);
        return openApi;
    }

}
