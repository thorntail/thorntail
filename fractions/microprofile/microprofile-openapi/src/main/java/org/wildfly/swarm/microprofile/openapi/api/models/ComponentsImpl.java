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

package org.wildfly.swarm.microprofile.openapi.api.models;

import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.microprofile.openapi.models.Components;
import org.eclipse.microprofile.openapi.models.callbacks.Callback;
import org.eclipse.microprofile.openapi.models.examples.Example;
import org.eclipse.microprofile.openapi.models.headers.Header;
import org.eclipse.microprofile.openapi.models.links.Link;
import org.eclipse.microprofile.openapi.models.media.Schema;
import org.eclipse.microprofile.openapi.models.parameters.Parameter;
import org.eclipse.microprofile.openapi.models.parameters.RequestBody;
import org.eclipse.microprofile.openapi.models.responses.APIResponse;
import org.eclipse.microprofile.openapi.models.security.SecurityScheme;

/**
 * An implementation of the {@link Components} OpenAPI model interface.
 */
public class ComponentsImpl extends ExtensibleImpl implements Components, ModelImpl {

    private Map<String, Schema> schemas;
    private Map<String, APIResponse> responses;
    private Map<String, Parameter> parameters;
    private Map<String, Example> examples;
    private Map<String, RequestBody> requestBodies;
    private Map<String, Header> headers;
    private Map<String, SecurityScheme> securitySchemes;
    private Map<String, Link> links;
    private Map<String, Callback> callbacks;

    /**
     * @see org.eclipse.microprofile.openapi.models.Components#getSchemas()
     */
    @Override
    public Map<String, Schema> getSchemas() {
        return this.schemas;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.Components#setSchemas(java.util.Map)
     */
    @Override
    public void setSchemas(Map<String, Schema> schemas) {
        this.schemas = schemas;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.Components#schemas(java.util.Map)
     */
    @Override
    public Components schemas(Map<String, Schema> schemas) {
        this.schemas = schemas;
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.Components#addSchema(java.lang.String, org.eclipse.microprofile.openapi.models.media.Schema)
     */
    @Override
    public Components addSchema(String key, Schema schema) {
        if (this.schemas == null) {
            this.schemas = new LinkedHashMap<>();
        }
        this.schemas.put(key, schema);
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.Components#getResponses()
     */
    @Override
    public Map<String, APIResponse> getResponses() {
        return this.responses;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.Components#setResponses(java.util.Map)
     */
    @Override
    public void setResponses(Map<String, APIResponse> responses) {
        this.responses = responses;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.Components#responses(java.util.Map)
     */
    @Override
    public Components responses(Map<String, APIResponse> responses) {
        this.responses = responses;
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.Components#addResponse(java.lang.String, org.eclipse.microprofile.openapi.models.responses.APIResponse)
     */
    @Override
    public Components addResponse(String key, APIResponse response) {
        if (this.responses == null) {
            this.responses = new LinkedHashMap<>();
        }
        this.responses.put(key, response);
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.Components#getParameters()
     */
    @Override
    public Map<String, Parameter> getParameters() {
        return this.parameters;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.Components#setParameters(java.util.Map)
     */
    @Override
    public void setParameters(Map<String, Parameter> parameters) {
        this.parameters = parameters;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.Components#parameters(java.util.Map)
     */
    @Override
    public Components parameters(Map<String, Parameter> parameters) {
        this.parameters = parameters;
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.Components#addParameter(java.lang.String, org.eclipse.microprofile.openapi.models.parameters.Parameter)
     */
    @Override
    public Components addParameter(String key, Parameter parameter) {
        if (this.parameters == null) {
            this.parameters = new LinkedHashMap<>();
        }
        this.parameters.put(key, parameter);
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.Components#getExamples()
     */
    @Override
    public Map<String, Example> getExamples() {
        return this.examples;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.Components#setExamples(java.util.Map)
     */
    @Override
    public void setExamples(Map<String, Example> examples) {
        this.examples = examples;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.Components#examples(java.util.Map)
     */
    @Override
    public Components examples(Map<String, Example> examples) {
        this.examples = examples;
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.Components#addExample(java.lang.String, org.eclipse.microprofile.openapi.models.examples.Example)
     */
    @Override
    public Components addExample(String key, Example example) {
        if (this.examples == null) {
            this.examples = new LinkedHashMap<>();
        }
        this.examples.put(key, example);
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.Components#getRequestBodies()
     */
    @Override
    public Map<String, RequestBody> getRequestBodies() {
        return this.requestBodies;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.Components#setRequestBodies(java.util.Map)
     */
    @Override
    public void setRequestBodies(Map<String, RequestBody> requestBodies) {
        this.requestBodies = requestBodies;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.Components#requestBodies(java.util.Map)
     */
    @Override
    public Components requestBodies(Map<String, RequestBody> requestBodies) {
        this.requestBodies = requestBodies;
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.Components#addRequestBody(java.lang.String, org.eclipse.microprofile.openapi.models.parameters.RequestBody)
     */
    @Override
    public Components addRequestBody(String key, RequestBody requestBody) {
        if (this.requestBodies == null) {
            this.requestBodies = new LinkedHashMap<>();
        }
        this.requestBodies.put(key, requestBody);
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.Components#getHeaders()
     */
    @Override
    public Map<String, Header> getHeaders() {
        return this.headers;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.Components#setHeaders(java.util.Map)
     */
    @Override
    public void setHeaders(Map<String, Header> headers) {
        this.headers = headers;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.Components#headers(java.util.Map)
     */
    @Override
    public Components headers(Map<String, Header> headers) {
        this.headers = headers;
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.Components#addHeader(java.lang.String, org.eclipse.microprofile.openapi.models.headers.Header)
     */
    @Override
    public Components addHeader(String key, Header header) {
        if (this.headers == null) {
            this.headers = new LinkedHashMap<>();
        }
        this.headers.put(key, header);
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.Components#getSecuritySchemes()
     */
    @Override
    public Map<String, SecurityScheme> getSecuritySchemes() {
        return this.securitySchemes;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.Components#setSecuritySchemes(java.util.Map)
     */
    @Override
    public void setSecuritySchemes(Map<String, SecurityScheme> securitySchemes) {
        this.securitySchemes = securitySchemes;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.Components#securitySchemes(java.util.Map)
     */
    @Override
    public Components securitySchemes(Map<String, SecurityScheme> securitySchemes) {
        this.securitySchemes = securitySchemes;
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.Components#addSecurityScheme(java.lang.String, org.eclipse.microprofile.openapi.models.security.SecurityScheme)
     */
    @Override
    public Components addSecurityScheme(String key, SecurityScheme securityScheme) {
        if (this.securitySchemes == null) {
            this.securitySchemes = new LinkedHashMap<>();
        }
        this.securitySchemes.put(key, securityScheme);
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.Components#getLinks()
     */
    @Override
    public Map<String, Link> getLinks() {
        return this.links;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.Components#setLinks(java.util.Map)
     */
    @Override
    public void setLinks(Map<String, Link> links) {
        this.links = links;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.Components#links(java.util.Map)
     */
    @Override
    public Components links(Map<String, Link> links) {
        this.links = links;
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.Components#addLink(java.lang.String, org.eclipse.microprofile.openapi.models.links.Link)
     */
    @Override
    public Components addLink(String key, Link link) {
        if (this.links == null) {
            this.links = new LinkedHashMap<>();
        }
        this.links.put(key, link);
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.Components#getCallbacks()
     */
    @Override
    public Map<String, Callback> getCallbacks() {
        return this.callbacks;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.Components#setCallbacks(java.util.Map)
     */
    @Override
    public void setCallbacks(Map<String, Callback> callbacks) {
        this.callbacks = callbacks;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.Components#callbacks(java.util.Map)
     */
    @Override
    public Components callbacks(Map<String, Callback> callbacks) {
        this.callbacks = callbacks;
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.Components#addCallback(java.lang.String, org.eclipse.microprofile.openapi.models.callbacks.Callback)
     */
    @Override
    public Components addCallback(String key, Callback callback) {
        if (this.callbacks == null) {
            this.callbacks = new LinkedHashMap<>();
        }
        this.callbacks.put(key, callback);
        return this;
    }

}