/**
 * Copyright 2017 Red Hat, Inc, and individual contributors.
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

package org.jboss.unimbus.openapi.impl.api.models;

import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.microprofile.openapi.models.PathItem;
import org.eclipse.microprofile.openapi.models.Paths;

/**
 * An implementation of the {@link Paths} OpenAPI model interface.
 */
public class PathsImpl extends LinkedHashMap<String, PathItem> implements Paths, ModelImpl {

    private static final long serialVersionUID = 8872198998600578356L;

    private Map<String, Object> extensions;

    /**
     * @see org.eclipse.microprofile.openapi.models.Extensible#getExtensions()
     */
    @Override
    public Map<String, Object> getExtensions() {
        return this.extensions;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.Extensible#addExtension(String, Object)
     */
    @Override
    public void addExtension(String name, Object value) {
        if (extensions == null) {
            this.extensions = new LinkedHashMap<>();
        }
        this.extensions.put(name, value);
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.Extensible#setExtensions(Map)
     */
    @Override
    public void setExtensions(Map<String, Object> extensions) {
        this.extensions = extensions;
    }

    /**
     * @see Paths#addPathItem(String, PathItem)
     */
    @Override
    public Paths addPathItem(String name, PathItem item) {
        this.put(name, item);
        return this;
    }

}