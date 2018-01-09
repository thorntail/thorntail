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

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.microprofile.openapi.OASFilter;
import org.eclipse.microprofile.openapi.models.OpenAPI;

/**
 * @author eric.wittmann@gmail.com
 */
public class FilterUtil {

    /**
     * Apply the given filter to the given model.
     * @param filter
     * @param model
     */
    public static final OpenAPI applyFilter(OASFilter filter, OpenAPI model) {
        filterMap(model.getPaths(), item -> { return filter.filterPathItem(item); });
        if (model.getComponents() != null) {
            filterMap(model.getComponents().getCallbacks(), item -> { return filter.filterCallback(item); });
            filterMap(model.getComponents().getHeaders(), item -> { return filter.filterHeader(item); });
            filterMap(model.getComponents().getLinks(), item -> { return filter.filterLink(item); });
            filterMap(model.getComponents().getParameters(), item -> { return filter.filterParameter(item); });
            filterMap(model.getComponents().getRequestBodies(), item -> { return filter.filterRequestBody(item); });
            filterMap(model.getComponents().getResponses(), item -> { return filter.filterAPIResponse(item); });
            filterMap(model.getComponents().getSchemas(), item -> { return filter.filterSchema(item); });
            filterMap(model.getComponents().getSecuritySchemes(), item -> { return filter.filterSecurityScheme(item); });
        }

        filter.filterOpenAPI(model);
        return model;
    }

    /**
     * Filters a map of items.
     * @param items
     * @param filter
     */
    private static final <T> void filterMap(Map<String, T> items, Filterable<T> filter) {
        if (items == null || items.isEmpty()) {
            return;
        }

        Set<String> keys = new HashSet<String>(items.keySet());
        for (String key : keys) {
            T item = items.get(key);
            item = filter.filter(item);
            if (item == null) {
                items.remove(key);
            } else {
                items.put(key, item);
            }
        }
    }

    @FunctionalInterface
    public static interface Filterable<V> {
        V filter(V item);
    }

}
