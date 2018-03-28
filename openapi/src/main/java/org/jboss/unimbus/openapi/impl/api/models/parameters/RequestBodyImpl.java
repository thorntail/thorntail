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

package org.jboss.unimbus.openapi.impl.api.models.parameters;

import org.eclipse.microprofile.openapi.models.media.Content;
import org.eclipse.microprofile.openapi.models.parameters.RequestBody;
import org.jboss.unimbus.openapi.impl.api.models.ExtensibleImpl;
import org.jboss.unimbus.openapi.impl.api.models.ModelImpl;
import org.jboss.unimbus.openapi.impl.OpenApiConstants;

/**
 * An implementation of the {@link RequestBody} OpenAPI model interface.
 */
public class RequestBodyImpl extends ExtensibleImpl implements RequestBody, ModelImpl {

    private String $ref;

    private String description;

    private Content content;

    private Boolean required;

    /**
     * @see org.eclipse.microprofile.openapi.models.Reference#getRef()
     */
    @Override
    public String getRef() {
        return this.$ref;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.Reference#setRef(String)
     */
    @Override
    public void setRef(String ref) {
        if (ref != null && !ref.contains("/")) {
            ref = OpenApiConstants.REF_PREFIX_REQUEST_BODY + ref;
        }
        this.$ref = ref;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.Reference#ref(String)
     */
    @Override
    public RequestBody ref(String ref) {
        setRef(ref);
        return this;
    }

    /**
     * @see RequestBody#getDescription()
     */
    @Override
    public String getDescription() {
        return this.description;
    }

    /**
     * @see RequestBody#setDescription(String)
     */
    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @see RequestBody#description(String)
     */
    @Override
    public RequestBody description(String description) {
        this.description = description;
        return this;
    }

    /**
     * @see RequestBody#getContent()
     */
    @Override
    public Content getContent() {
        return this.content;
    }

    /**
     * @see RequestBody#setContent(Content)
     */
    @Override
    public void setContent(Content content) {
        this.content = content;
    }

    /**
     * @see RequestBody#content(Content)
     */
    @Override
    public RequestBody content(Content content) {
        this.content = content;
        return this;
    }

    /**
     * @see RequestBody#getRequired()
     */
    @Override
    public Boolean getRequired() {
        return this.required;
    }

    /**
     * @see RequestBody#setRequired(Boolean)
     */
    @Override
    public void setRequired(Boolean required) {
        this.required = required;
    }

    /**
     * @see RequestBody#required(Boolean)
     */
    @Override
    public RequestBody required(Boolean required) {
        this.required = required;
        return this;
    }

}