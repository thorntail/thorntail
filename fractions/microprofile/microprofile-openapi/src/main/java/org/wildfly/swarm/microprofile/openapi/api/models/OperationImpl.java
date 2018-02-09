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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.microprofile.openapi.models.ExternalDocumentation;
import org.eclipse.microprofile.openapi.models.Operation;
import org.eclipse.microprofile.openapi.models.callbacks.Callback;
import org.eclipse.microprofile.openapi.models.parameters.Parameter;
import org.eclipse.microprofile.openapi.models.parameters.RequestBody;
import org.eclipse.microprofile.openapi.models.responses.APIResponses;
import org.eclipse.microprofile.openapi.models.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.models.servers.Server;

/**
 * An implementation of the {@link ExternalDocumentation} OpenAPI model interface.
 */
public class OperationImpl extends ExtensibleImpl implements Operation, ModelImpl {

    private List<String> tags;
    private String summary;
    private String description;
    private ExternalDocumentation externalDocs;
    private String operationId;
    private List<Parameter> parameters;
    private RequestBody requestBody;
    private APIResponses responses;
    private Map<String, Callback> callbacks;
    private Boolean deprecated;
    private List<SecurityRequirement> security;
    private List<Server> servers;

    /**
     * @see org.eclipse.microprofile.openapi.models.Operation#getTags()
     */
    @Override
    public List<String> getTags() {
        return this.tags;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.Operation#setTags(java.util.List)
     */
    @Override
    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.Operation#tags(java.util.List)
     */
    @Override
    public Operation tags(List<String> tags) {
        this.tags = tags;
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.Operation#addTag(java.lang.String)
     */
    @Override
    public Operation addTag(String tag) {
        if (this.tags == null) {
            this.tags = new ArrayList<>();
        }
        this.tags.add(tag);
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.Operation#getSummary()
     */
    @Override
    public String getSummary() {
        return this.summary;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.Operation#setSummary(java.lang.String)
     */
    @Override
    public void setSummary(String summary) {
        this.summary = summary;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.Operation#summary(java.lang.String)
     */
    @Override
    public Operation summary(String summary) {
        this.summary = summary;
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.Operation#getDescription()
     */
    @Override
    public String getDescription() {
        return this.description;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.Operation#setDescription(java.lang.String)
     */
    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.Operation#description(java.lang.String)
     */
    @Override
    public Operation description(String description) {
        this.description = description;
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.Operation#getExternalDocs()
     */
    @Override
    public ExternalDocumentation getExternalDocs() {
        return this.externalDocs;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.Operation#setExternalDocs(org.eclipse.microprofile.openapi.models.ExternalDocumentation)
     */
    @Override
    public void setExternalDocs(ExternalDocumentation externalDocs) {
        this.externalDocs = externalDocs;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.Operation#externalDocs(org.eclipse.microprofile.openapi.models.ExternalDocumentation)
     */
    @Override
    public Operation externalDocs(ExternalDocumentation externalDocs) {
        this.externalDocs = externalDocs;
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.Operation#getOperationId()
     */
    @Override
    public String getOperationId() {
        return this.operationId;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.Operation#setOperationId(java.lang.String)
     */
    @Override
    public void setOperationId(String operationId) {
        this.operationId = operationId;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.Operation#operationId(java.lang.String)
     */
    @Override
    public Operation operationId(String operationId) {
        this.operationId = operationId;
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.Operation#getParameters()
     */
    @Override
    public List<Parameter> getParameters() {
        return this.parameters;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.Operation#setParameters(java.util.List)
     */
    @Override
    public void setParameters(List<Parameter> parameters) {
        this.parameters = parameters;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.Operation#parameters(java.util.List)
     */
    @Override
    public Operation parameters(List<Parameter> parameters) {
        this.parameters = parameters;
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.Operation#addParameter(org.eclipse.microprofile.openapi.models.parameters.Parameter)
     */
    @Override
    public Operation addParameter(Parameter parameter) {
        if (this.parameters == null) {
            this.parameters = new ArrayList<>();
        }
        this.parameters.add(parameter);
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.Operation#getRequestBody()
     */
    @Override
    public RequestBody getRequestBody() {
        return this.requestBody;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.Operation#setRequestBody(org.eclipse.microprofile.openapi.models.parameters.RequestBody)
     */
    @Override
    public void setRequestBody(RequestBody requestBody) {
        this.requestBody = requestBody;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.Operation#requestBody(org.eclipse.microprofile.openapi.models.parameters.RequestBody)
     */
    @Override
    public Operation requestBody(RequestBody requestBody) {
        this.requestBody = requestBody;
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.Operation#getResponses()
     */
    @Override
    public APIResponses getResponses() {
        return this.responses;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.Operation#setResponses(org.eclipse.microprofile.openapi.models.responses.APIResponses)
     */
    @Override
    public void setResponses(APIResponses responses) {
        this.responses = responses;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.Operation#responses(org.eclipse.microprofile.openapi.models.responses.APIResponses)
     */
    @Override
    public Operation responses(APIResponses responses) {
        this.responses = responses;
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.Operation#getCallbacks()
     */
    @Override
    public Map<String, Callback> getCallbacks() {
        return this.callbacks;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.Operation#setCallbacks(java.util.Map)
     */
    @Override
    public void setCallbacks(Map<String, Callback> callbacks) {
        this.callbacks = callbacks;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.Operation#callbacks(java.util.Map)
     */
    @Override
    public Operation callbacks(Map<String, Callback> callbacks) {
        this.callbacks = callbacks;
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.Operation#getDeprecated()
     */
    @Override
    public Boolean getDeprecated() {
        return this.deprecated;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.Operation#setDeprecated(java.lang.Boolean)
     */
    @Override
    public void setDeprecated(Boolean deprecated) {
        this.deprecated = deprecated;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.Operation#deprecated(java.lang.Boolean)
     */
    @Override
    public Operation deprecated(Boolean deprecated) {
        this.deprecated = deprecated;
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.Operation#getSecurity()
     */
    @Override
    public List<SecurityRequirement> getSecurity() {
        return this.security;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.Operation#setSecurity(java.util.List)
     */
    @Override
    public void setSecurity(List<SecurityRequirement> security) {
        this.security = security;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.Operation#security(java.util.List)
     */
    @Override
    public Operation security(List<SecurityRequirement> security) {
        this.security = security;
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.Operation#addSecurityRequirement(org.eclipse.microprofile.openapi.models.security.SecurityRequirement)
     */
    @Override
    public Operation addSecurityRequirement(SecurityRequirement securityRequirement) {
        if (this.security == null) {
            this.security = new ArrayList<>();
        }
        this.security.add(securityRequirement);
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.Operation#getServers()
     */
    @Override
    public List<Server> getServers() {
        return this.servers;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.Operation#setServers(java.util.List)
     */
    @Override
    public void setServers(List<Server> servers) {
        this.servers = servers;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.Operation#servers(java.util.List)
     */
    @Override
    public Operation servers(List<Server> servers) {
        this.servers = servers;
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.Operation#addServer(org.eclipse.microprofile.openapi.models.servers.Server)
     */
    @Override
    public Operation addServer(Server server) {
        if (this.servers == null) {
            this.servers = new ArrayList<>();
        }
        this.servers.add(server);
        return this;
    }

}