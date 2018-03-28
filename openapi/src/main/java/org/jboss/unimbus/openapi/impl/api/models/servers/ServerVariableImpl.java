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

package org.jboss.unimbus.openapi.impl.api.models.servers;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.microprofile.openapi.models.servers.ServerVariable;
import org.jboss.unimbus.openapi.impl.api.models.ExtensibleImpl;
import org.jboss.unimbus.openapi.impl.api.models.ModelImpl;

/**
 * An implementation of the {@link ServerVariable} OpenAPI model interface.
 */
public class ServerVariableImpl extends ExtensibleImpl implements ServerVariable, ModelImpl {

    private List<String> enumeration;

    private String defaultValue;

    private String description;

    /**
     * @see ServerVariable#getEnumeration()
     */
    @Override
    public List<String> getEnumeration() {
        return this.enumeration;
    }

    /**
     * @see ServerVariable#setEnumeration(List)
     */
    @Override
    public void setEnumeration(List<String> enumeration) {
        this.enumeration = enumeration;
    }

    /**
     * @see ServerVariable#enumeration(List)
     */
    @Override
    public ServerVariable enumeration(List<String> enumeration) {
        this.enumeration = enumeration;
        return this;
    }

    /**
     * @see ServerVariable#addEnumeration(String)
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
     * @see ServerVariable#getDefaultValue()
     */
    @Override
    public String getDefaultValue() {
        return this.defaultValue;
    }

    /**
     * @see ServerVariable#setDefaultValue(String)
     */
    @Override
    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    /**
     * @see ServerVariable#defaultValue(String)
     */
    @Override
    public ServerVariable defaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
        return this;
    }

    /**
     * @see ServerVariable#getDescription()
     */
    @Override
    public String getDescription() {
        return this.description;
    }

    /**
     * @see ServerVariable#setDescription(String)
     */
    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @see ServerVariable#description(String)
     */
    @Override
    public ServerVariable description(String description) {
        this.description = description;
        return this;
    }

}