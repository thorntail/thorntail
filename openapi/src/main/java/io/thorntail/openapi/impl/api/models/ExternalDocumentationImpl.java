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

package io.thorntail.openapi.impl.api.models;

import org.eclipse.microprofile.openapi.models.ExternalDocumentation;

/**
 * An implementation of the {@link ExternalDocumentation} OpenAPI model interface.
 */
public class ExternalDocumentationImpl extends ExtensibleImpl implements ExternalDocumentation, ModelImpl {

    private String description;

    private String url;

    /**
     * @see ExternalDocumentation#getDescription()
     */
    @Override
    public String getDescription() {
        return this.description;
    }

    /**
     * @see ExternalDocumentation#setDescription(String)
     */
    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @see ExternalDocumentation#description(String)
     */
    @Override
    public ExternalDocumentation description(String description) {
        this.description = description;
        return this;
    }

    /**
     * @see ExternalDocumentation#getUrl()
     */
    @Override
    public String getUrl() {
        return this.url;
    }

    /**
     * @see ExternalDocumentation#setUrl(String)
     */
    @Override
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * @see ExternalDocumentation#url(String)
     */
    @Override
    public ExternalDocumentation url(String url) {
        this.url = url;
        return this;
    }

}