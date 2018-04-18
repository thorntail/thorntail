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

package io.thorntail.openapi.impl.api.models.servers;

import java.util.LinkedHashMap;
import java.util.Map;

import io.thorntail.openapi.impl.api.models.ModelImpl;
import org.eclipse.microprofile.openapi.models.servers.ServerVariable;
import org.eclipse.microprofile.openapi.models.servers.ServerVariables;

/**
 * An implementation of the {@link ServerVariables} OpenAPI model interface.
 */
public class ServerVariablesImpl extends LinkedHashMap<String, ServerVariable> implements ServerVariables, ModelImpl {

    private static final long serialVersionUID = -7724841358483233927L;

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
     * @see ServerVariables#addServerVariable(String, ServerVariable)
     */
    @Override
    public ServerVariables addServerVariable(String name, ServerVariable serverVariable) {
        this.put(name, serverVariable);
        return this;
    }

}