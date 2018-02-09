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

package org.wildfly.swarm.microprofile.openapi.api.models.responses;

import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.microprofile.openapi.models.headers.Header;
import org.eclipse.microprofile.openapi.models.links.Link;
import org.eclipse.microprofile.openapi.models.media.Content;
import org.eclipse.microprofile.openapi.models.responses.APIResponse;
import org.wildfly.swarm.microprofile.openapi.api.models.ExtensibleImpl;
import org.wildfly.swarm.microprofile.openapi.api.models.ModelImpl;
import org.wildfly.swarm.microprofile.openapi.runtime.OpenApiConstants;

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
     * @see org.eclipse.microprofile.openapi.models.Reference#setRef(java.lang.String)
     */
    @Override
    public void setRef(String ref) {
        if (ref != null && !ref.contains("/")) {
            ref = OpenApiConstants.REF_PREFIX_API_RESPONSE + ref;
        }
        this.$ref = ref;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.Reference#ref(java.lang.String)
     */
    @Override
    public APIResponse ref(String ref) {
        setRef(ref);
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.responses.APIResponse#getDescription()
     */
    @Override
    public String getDescription() {
        return this.description;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.responses.APIResponse#setDescription(java.lang.String)
     */
    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.responses.APIResponse#description(java.lang.String)
     */
    @Override
    public APIResponse description(String description) {
        this.description = description;
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.responses.APIResponse#getHeaders()
     */
    @Override
    public Map<String, Header> getHeaders() {
        return this.headers;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.responses.APIResponse#setHeaders(java.util.Map)
     */
    @Override
    public void setHeaders(Map<String, Header> headers) {
        this.headers = headers;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.responses.APIResponse#headers(java.util.Map)
     */
    @Override
    public APIResponse headers(Map<String, Header> headers) {
        this.headers = headers;
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.responses.APIResponse#addHeader(java.lang.String, org.eclipse.microprofile.openapi.models.headers.Header)
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
     * @see org.eclipse.microprofile.openapi.models.responses.APIResponse#getContent()
     */
    @Override
    public Content getContent() {
        return this.content;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.responses.APIResponse#setContent(org.eclipse.microprofile.openapi.models.media.Content)
     */
    @Override
    public void setContent(Content content) {
        this.content = content;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.responses.APIResponse#content(org.eclipse.microprofile.openapi.models.media.Content)
     */
    @Override
    public APIResponse content(Content content) {
        this.content = content;
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.responses.APIResponse#getLinks()
     */
    @Override
    public Map<String, Link> getLinks() {
        return this.links;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.responses.APIResponse#setLinks(java.util.Map)
     */
    @Override
    public void setLinks(Map<String, Link> links) {
        this.links = links;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.responses.APIResponse#links(java.util.Map)
     */
    @Override
    public APIResponse links(Map<String, Link> links) {
        this.links = links;
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.responses.APIResponse#addLink(java.lang.String, org.eclipse.microprofile.openapi.models.links.Link)
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