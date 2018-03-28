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

package org.jboss.unimbus.openapi.impl.api.models;

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
     * @see Operation#getTags()
     */
    @Override
    public List<String> getTags() {
        return this.tags;
    }

    /**
     * @see Operation#setTags(List)
     */
    @Override
    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    /**
     * @see Operation#tags(List)
     */
    @Override
    public Operation tags(List<String> tags) {
        this.tags = tags;
        return this;
    }

    /**
     * @see Operation#addTag(String)
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
     * @see Operation#getSummary()
     */
    @Override
    public String getSummary() {
        return this.summary;
    }

    /**
     * @see Operation#setSummary(String)
     */
    @Override
    public void setSummary(String summary) {
        this.summary = summary;
    }

    /**
     * @see Operation#summary(String)
     */
    @Override
    public Operation summary(String summary) {
        this.summary = summary;
        return this;
    }

    /**
     * @see Operation#getDescription()
     */
    @Override
    public String getDescription() {
        return this.description;
    }

    /**
     * @see Operation#setDescription(String)
     */
    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @see Operation#description(String)
     */
    @Override
    public Operation description(String description) {
        this.description = description;
        return this;
    }

    /**
     * @see Operation#getExternalDocs()
     */
    @Override
    public ExternalDocumentation getExternalDocs() {
        return this.externalDocs;
    }

    /**
     * @see Operation#setExternalDocs(ExternalDocumentation)
     */
    @Override
    public void setExternalDocs(ExternalDocumentation externalDocs) {
        this.externalDocs = externalDocs;
    }

    /**
     * @see Operation#externalDocs(ExternalDocumentation)
     */
    @Override
    public Operation externalDocs(ExternalDocumentation externalDocs) {
        this.externalDocs = externalDocs;
        return this;
    }

    /**
     * @see Operation#getOperationId()
     */
    @Override
    public String getOperationId() {
        return this.operationId;
    }

    /**
     * @see Operation#setOperationId(String)
     */
    @Override
    public void setOperationId(String operationId) {
        this.operationId = operationId;
    }

    /**
     * @see Operation#operationId(String)
     */
    @Override
    public Operation operationId(String operationId) {
        this.operationId = operationId;
        return this;
    }

    /**
     * @see Operation#getParameters()
     */
    @Override
    public List<Parameter> getParameters() {
        return this.parameters;
    }

    /**
     * @see Operation#setParameters(List)
     */
    @Override
    public void setParameters(List<Parameter> parameters) {
        this.parameters = parameters;
    }

    /**
     * @see Operation#parameters(List)
     */
    @Override
    public Operation parameters(List<Parameter> parameters) {
        this.parameters = parameters;
        return this;
    }

    /**
     * @see Operation#addParameter(Parameter)
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
     * @see Operation#getRequestBody()
     */
    @Override
    public RequestBody getRequestBody() {
        return this.requestBody;
    }

    /**
     * @see Operation#setRequestBody(RequestBody)
     */
    @Override
    public void setRequestBody(RequestBody requestBody) {
        this.requestBody = requestBody;
    }

    /**
     * @see Operation#requestBody(RequestBody)
     */
    @Override
    public Operation requestBody(RequestBody requestBody) {
        this.requestBody = requestBody;
        return this;
    }

    /**
     * @see Operation#getResponses()
     */
    @Override
    public APIResponses getResponses() {
        return this.responses;
    }

    /**
     * @see Operation#setResponses(APIResponses)
     */
    @Override
    public void setResponses(APIResponses responses) {
        this.responses = responses;
    }

    /**
     * @see Operation#responses(APIResponses)
     */
    @Override
    public Operation responses(APIResponses responses) {
        this.responses = responses;
        return this;
    }

    /**
     * @see Operation#getCallbacks()
     */
    @Override
    public Map<String, Callback> getCallbacks() {
        return this.callbacks;
    }

    /**
     * @see Operation#setCallbacks(Map)
     */
    @Override
    public void setCallbacks(Map<String, Callback> callbacks) {
        this.callbacks = callbacks;
    }

    /**
     * @see Operation#callbacks(Map)
     */
    @Override
    public Operation callbacks(Map<String, Callback> callbacks) {
        this.callbacks = callbacks;
        return this;
    }

    /**
     * @see Operation#getDeprecated()
     */
    @Override
    public Boolean getDeprecated() {
        return this.deprecated;
    }

    /**
     * @see Operation#setDeprecated(Boolean)
     */
    @Override
    public void setDeprecated(Boolean deprecated) {
        this.deprecated = deprecated;
    }

    /**
     * @see Operation#deprecated(Boolean)
     */
    @Override
    public Operation deprecated(Boolean deprecated) {
        this.deprecated = deprecated;
        return this;
    }

    /**
     * @see Operation#getSecurity()
     */
    @Override
    public List<SecurityRequirement> getSecurity() {
        return this.security;
    }

    /**
     * @see Operation#setSecurity(List)
     */
    @Override
    public void setSecurity(List<SecurityRequirement> security) {
        this.security = security;
    }

    /**
     * @see Operation#security(List)
     */
    @Override
    public Operation security(List<SecurityRequirement> security) {
        this.security = security;
        return this;
    }

    /**
     * @see Operation#addSecurityRequirement(SecurityRequirement)
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
     * @see Operation#getServers()
     */
    @Override
    public List<Server> getServers() {
        return this.servers;
    }

    /**
     * @see Operation#setServers(List)
     */
    @Override
    public void setServers(List<Server> servers) {
        this.servers = servers;
    }

    /**
     * @see Operation#servers(List)
     */
    @Override
    public Operation servers(List<Server> servers) {
        this.servers = servers;
        return this;
    }

    /**
     * @see Operation#addServer(Server)
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