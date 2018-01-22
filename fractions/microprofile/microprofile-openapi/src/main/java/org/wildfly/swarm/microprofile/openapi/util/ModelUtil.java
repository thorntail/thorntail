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

package org.wildfly.swarm.microprofile.openapi.util;

import org.eclipse.microprofile.openapi.models.Components;
import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.eclipse.microprofile.openapi.models.Paths;
import org.wildfly.swarm.microprofile.openapi.models.ComponentsImpl;
import org.wildfly.swarm.microprofile.openapi.models.OpenAPIImpl;
import org.wildfly.swarm.microprofile.openapi.models.PathsImpl;

/**
 * Class with some convenience methods useful for working with the OAI data model.
 * @author eric.wittmann@gmail.com
 */
public class ModelUtil {

    /**
     * Constructor.
     */
    private ModelUtil() {
    }

    /**
     * Gets the {@link Components} from the OAI model.  If it doesn't exist, creates it.
     * @param openApi
     */
    public static Components components(OpenAPI openApi) {
        if (openApi.getComponents() == null) {
            openApi.setComponents(new ComponentsImpl());
        }
        return openApi.getComponents();
    }

    /**
     * Gets the {@link Paths} from the OAI model.  If it doesn't exist, creates it.
     * @param openApi
     */
    public static Paths paths(OpenAPIImpl openApi) {
        if (openApi.getPaths() == null) {
            openApi.setPaths(new PathsImpl());
        }
        return openApi.getPaths();
    }

}
