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

package org.wildfly.swarm.microprofile.openapi.api.models.servers;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.microprofile.openapi.models.servers.ServerVariable;
import org.wildfly.swarm.microprofile.openapi.api.models.ExtensibleImpl;
import org.wildfly.swarm.microprofile.openapi.api.models.ModelImpl;

/**
 * An implementation of the {@link ServerVariable} OpenAPI model interface.
 */
public class ServerVariableImpl extends ExtensibleImpl implements ServerVariable, ModelImpl {

    private List<String> enumeration;
    private String defaultValue;
    private String description;

    /**
     * @see org.eclipse.microprofile.openapi.models.servers.ServerVariable#getEnumeration()
     */
    @Override
    public List<String> getEnumeration() {
        return this.enumeration;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.servers.ServerVariable#setEnumeration(java.util.List)
     */
    @Override
    public void setEnumeration(List<String> enumeration) {
        this.enumeration = enumeration;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.servers.ServerVariable#enumeration(java.util.List)
     */
    @Override
    public ServerVariable enumeration(List<String> enumeration) {
        this.enumeration = enumeration;
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.servers.ServerVariable#addEnumeration(java.lang.String)
     */
    @Override
    public ServerVariable addEnumeration(String enumeration) {
        if (this.enumeration == null) {
            this.enumeration = new ArrayList<>();
        }
        this.enumeration.add(enumeration);
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.servers.ServerVariable#getDefaultValue()
     */
    @Override
    public String getDefaultValue() {
        return this.defaultValue;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.servers.ServerVariable#setDefaultValue(java.lang.String)
     */
    @Override
    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.servers.ServerVariable#defaultValue(java.lang.String)
     */
    @Override
    public ServerVariable defaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.servers.ServerVariable#getDescription()
     */
    @Override
    public String getDescription() {
        return this.description;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.servers.ServerVariable#setDescription(java.lang.String)
     */
    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.servers.ServerVariable#description(java.lang.String)
     */
    @Override
    public ServerVariable description(String description) {
        this.description = description;
        return this;
    }

}