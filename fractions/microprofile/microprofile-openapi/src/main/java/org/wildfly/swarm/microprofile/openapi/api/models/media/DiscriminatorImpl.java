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

package org.wildfly.swarm.microprofile.openapi.api.models.media;

import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.microprofile.openapi.models.media.Discriminator;
import org.wildfly.swarm.microprofile.openapi.api.models.ModelImpl;

/**
 * An implementation of the {@link Discriminator} OpenAPI model interface.
 */
public class DiscriminatorImpl implements Discriminator, ModelImpl {

    private String propertyName;
    private Map<String, String> mapping;

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Discriminator#propertyName(java.lang.String)
     */
    @Override
    public Discriminator propertyName(String propertyName) {
        this.propertyName = propertyName;
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Discriminator#getPropertyName()
     */
    @Override
    public String getPropertyName() {
        return this.propertyName;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Discriminator#setPropertyName(java.lang.String)
     */
    @Override
    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Discriminator#addMapping(java.lang.String, java.lang.String)
     */
    @Override
    public Discriminator addMapping(String name, String value) {
        if (this.mapping == null) {
            this.mapping = new LinkedHashMap<>();
        }
        this.mapping.put(name, value);
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Discriminator#mapping(java.util.Map)
     */
    @Override
    public Discriminator mapping(Map<String, String> mapping) {
        this.mapping = mapping;
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Discriminator#getMapping()
     */
    @Override
    public Map<String, String> getMapping() {
        return this.mapping;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Discriminator#setMapping(java.util.Map)
     */
    @Override
    public void setMapping(Map<String, String> mapping) {
        this.mapping = mapping;
    }

}