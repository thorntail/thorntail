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

package io.thorntail.openapi.impl.api.models.responses;

import java.util.LinkedHashMap;
import java.util.Map;

import io.thorntail.openapi.impl.OpenApiConstants;
import io.thorntail.openapi.impl.api.models.ExtensibleImpl;
import io.thorntail.openapi.impl.api.models.ModelImpl;
import org.eclipse.microprofile.openapi.models.headers.Header;
import org.eclipse.microprofile.openapi.models.links.Link;
import org.eclipse.microprofile.openapi.models.media.Content;
import org.eclipse.microprofile.openapi.models.responses.APIResponse;

/**
 * An implementation of the {@link APIResponse} OpenAPI model interface.
 */
public class APIResponseImpl extends ExtensibleImpl implements APIResponse, ModelImpl {

    private String $ref;

    private String description;

    private Map<String, Header> headers;

    private Content content;

    private Map<String, Link> links;

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
            ref = OpenApiConstants.REF_PREFIX_API_RESPONSE + ref;
        }
        this.$ref = ref;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.Reference#ref(String)
     */
    @Override
    public APIResponse ref(String ref) {
        setRef(ref);
        return this;
    }

    /**
     * @see APIResponse#getDescription()
     */
    @Override
    public String getDescription() {
        return this.description;
    }

    /**
     * @see APIResponse#setDescription(String)
     */
    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @see APIResponse#description(String)
     */
    @Override
    public APIResponse description(String description) {
        this.description = description;
        return this;
    }

    /**
     * @see APIResponse#getHeaders()
     */
    @Override
    public Map<String, Header> getHeaders() {
        return this.headers;
    }

    /**
     * @see APIResponse#setHeaders(Map)
     */
    @Override
    public void setHeaders(Map<String, Header> headers) {
        this.headers = headers;
    }

    /**
     * @see APIResponse#headers(Map)
     */
    @Override
    public APIResponse headers(Map<String, Header> headers) {
        this.headers = headers;
        return this;
    }

    /**
     * @see APIResponse#addHeader(String, Header)
     */
    @Override
    public APIResponse addHeader(String name, Header header) {
        if (this.headers == null) {
            this.headers = new LinkedHashMap<>();
        }
        this.headers.put(name, header);
        return this;
    }

    /**
     * @see APIResponse#getContent()
     */
    @Override
    public Content getContent() {
        return this.content;
    }

    /**
     * @see APIResponse#setContent(Content)
     */
    @Override
    public void setContent(Content content) {
        this.content = content;
    }

    /**
     * @see APIResponse#content(Content)
     */
    @Override
    public APIResponse content(Content content) {
        this.content = content;
        return this;
    }

    /**
     * @see APIResponse#getLinks()
     */
    @Override
    public Map<String, Link> getLinks() {
        return this.links;
    }

    /**
     * @see APIResponse#setLinks(Map)
     */
    @Override
    public void setLinks(Map<String, Link> links) {
        this.links = links;
    }

    /**
     * @see APIResponse#links(Map)
     */
    @Override
    public APIResponse links(Map<String, Link> links) {
        this.links = links;
        return this;
    }

    /**
     * @see APIResponse#addLink(String, Link)
     */
    @Override
    public APIResponse addLink(String name, Link link) {
        if (this.links == null) {
            this.links = new LinkedHashMap<>();
        }
        this.links.put(name, link);
        return this;
    }

}