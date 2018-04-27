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

package io.thorntail.openapi.impl.api.models.tags;

import org.eclipse.microprofile.openapi.models.ExternalDocumentation;
import org.eclipse.microprofile.openapi.models.tags.Tag;
import io.thorntail.openapi.impl.api.models.ExtensibleImpl;
import io.thorntail.openapi.impl.api.models.ModelImpl;

/**
 * An implementation of the {@link Tag} OpenAPI model interface.
 */
public class TagImpl extends ExtensibleImpl implements Tag, ModelImpl {

    private String name;

    private String description;

    private ExternalDocumentation externalDocs;

    /**
     * @see Tag#getName()
     */
    @Override
    public String getName() {
        return this.name;
    }

    /**
     * @see Tag#setName(String)
     */
    @Override
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @see Tag#name(String)
     */
    @Override
    public Tag name(String name) {
        this.name = name;
        return this;
    }

    /**
     * @see Tag#getDescription()
     */
    @Override
    public String getDescription() {
        return this.description;
    }

    /**
     * @see Tag#setDescription(String)
     */
    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @see Tag#description(String)
     */
    @Override
    public Tag description(String description) {
        this.description = description;
        return this;
    }

    /**
     * @see Tag#getExternalDocs()
     */
    @Override
    public ExternalDocumentation getExternalDocs() {
        return this.externalDocs;
    }

    /**
     * @see Tag#setExternalDocs(ExternalDocumentation)
     */
    @Override
    public void setExternalDocs(ExternalDocumentation externalDocs) {
        this.externalDocs = externalDocs;
    }

    /**
     * @see Tag#externalDocs(ExternalDocumentation)
     */
    @Override
    public Tag externalDocs(ExternalDocumentation externalDocs) {
        this.externalDocs = externalDocs;
        return this;
    }

}