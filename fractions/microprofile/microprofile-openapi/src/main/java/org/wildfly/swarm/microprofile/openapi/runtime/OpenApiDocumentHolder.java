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

import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.wildfly.swarm.microprofile.openapi.models.OpenAPIImpl;

/**
 * Holds the final OpenAPI document produced during the startup of the app.
 * @author eric.wittmann@gmail.com
 */
public class OpenApiDocumentHolder {

    public static OpenAPI document = new OpenAPIImpl();

    /**
     * Constructor.
     */
    private OpenApiDocumentHolder() {
    }

}
