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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.microprofile.openapi.models.Operation;
import org.eclipse.microprofile.openapi.models.PathItem;
import org.eclipse.microprofile.openapi.models.parameters.Parameter;
import org.eclipse.microprofile.openapi.models.servers.Server;

/**
 * An implementation of the {@link PathItem} OpenAPI model interface.
 */
public class PathItemImpl extends ExtensibleImpl implements PathItem, ModelImpl {

    private String $ref;
    private String summary;
    private String description;
    private Operation get;
    private Operation put;
    private Operation post;
    private Operation delete;
    private Operation options;
    private Operation head;
    private Operation patch;
    private Operation trace;
    private List<Parameter> parameters;
    private List<Server> servers;

    /**
     * @see org.eclipse.microprofile.openapi.models.Reference#getRef()
     */
    @Override
    public String getRef() {
        return $ref;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.Reference#setRef(java.lang.String)
     */
    @Override
    public void setRef(String ref) {
        this.$ref = ref;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.Reference#ref(java.lang.String)
     */
    @Override
    public PathItem ref(String ref) {
        this.$ref = ref;
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.PathItem#getSummary()
     */
    @Override
    public String getSummary() {
        return this.summary;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.PathItem#setSummary(java.lang.String)
     */
    @Override
    public void setSummary(String summary) {
        this.summary = summary;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.PathItem#summary(java.lang.String)
     */
    @Override
    public PathItem summary(String summary) {
        this.summary = summary;
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.PathItem#getDescription()
     */
    @Override
    public String getDescription() {
        return this.description;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.PathItem#setDescription(java.lang.String)
     */
    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.PathItem#description(java.lang.String)
     */
    @Override
    public PathItem description(String description) {
        this.description = description;
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.PathItem#getGET()
     */
    @Override
    public Operation getGET() {
        return this.get;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.PathItem#setGET(org.eclipse.microprofile.openapi.models.Operation)
     */
    @Override
    public void setGET(Operation get) {
        this.get = get;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.PathItem#GET(org.eclipse.microprofile.openapi.models.Operation)
     */
    @Override
    public PathItem GET(Operation get) {
        this.get = get;
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.PathItem#getPUT()
     */
    @Override
    public Operation getPUT() {
        return this.put;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.PathItem#setPUT(org.eclipse.microprofile.openapi.models.Operation)
     */
    @Override
    public void setPUT(Operation put) {
        this.put = put;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.PathItem#PUT(org.eclipse.microprofile.openapi.models.Operation)
     */
    @Override
    public PathItem PUT(Operation put) {
        this.put = put;
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.PathItem#getPOST()
     */
    @Override
    public Operation getPOST() {
        return this.post;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.PathItem#setPOST(org.eclipse.microprofile.openapi.models.Operation)
     */
    @Override
    public void setPOST(Operation post) {
        this.post = post;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.PathItem#POST(org.eclipse.microprofile.openapi.models.Operation)
     */
    @Override
    public PathItem POST(Operation post) {
        this.post = post;
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.PathItem#getDELETE()
     */
    @Override
    public Operation getDELETE() {
        return this.delete;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.PathItem#setDELETE(org.eclipse.microprofile.openapi.models.Operation)
     */
    @Override
    public void setDELETE(Operation delete) {
        this.delete = delete;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.PathItem#DELETE(org.eclipse.microprofile.openapi.models.Operation)
     */
    @Override
    public PathItem DELETE(Operation delete) {
        this.delete = delete;
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.PathItem#getOPTIONS()
     */
    @Override
    public Operation getOPTIONS() {
        return this.options;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.PathItem#setOPTIONS(org.eclipse.microprofile.openapi.models.Operation)
     */
    @Override
    public void setOPTIONS(Operation options) {
        this.options = options;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.PathItem#OPTIONS(org.eclipse.microprofile.openapi.models.Operation)
     */
    @Override
    public PathItem OPTIONS(Operation options) {
        this.options = options;
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.PathItem#getHEAD()
     */
    @Override
    public Operation getHEAD() {
        return this.head;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.PathItem#setHEAD(org.eclipse.microprofile.openapi.models.Operation)
     */
    @Override
    public void setHEAD(Operation head) {
        this.head = head;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.PathItem#HEAD(org.eclipse.microprofile.openapi.models.Operation)
     */
    @Override
    public PathItem HEAD(Operation head) {
        this.head = head;
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.PathItem#getPATCH()
     */
    @Override
    public Operation getPATCH() {
        return this.patch;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.PathItem#setPATCH(org.eclipse.microprofile.openapi.models.Operation)
     */
    @Override
    public void setPATCH(Operation patch) {
        this.patch = patch;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.PathItem#PATCH(org.eclipse.microprofile.openapi.models.Operation)
     */
    @Override
    public PathItem PATCH(Operation patch) {
        this.patch = patch;
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.PathItem#getTRACE()
     */
    @Override
    public Operation getTRACE() {
        return this.trace;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.PathItem#setTRACE(org.eclipse.microprofile.openapi.models.Operation)
     */
    @Override
    public void setTRACE(Operation trace) {
        this.trace = trace;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.PathItem#TRACE(org.eclipse.microprofile.openapi.models.Operation)
     */
    @Override
    public PathItem TRACE(Operation trace) {
        this.trace = trace;
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.PathItem#readOperations()
     */
    @Override
    public List<Operation> readOperations() {
        List<Operation> ops = new ArrayList<>();
        addOperationToList(this.get, ops);
        addOperationToList(this.put, ops);
        addOperationToList(this.post, ops);
        addOperationToList(this.delete, ops);
        addOperationToList(this.options, ops);
        addOperationToList(this.head, ops);
        addOperationToList(this.patch, ops);
        addOperationToList(this.trace, ops);
        return ops;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.PathItem#readOperationsMap()
     */
    @Override
    public Map<HttpMethod, Operation> readOperationsMap() {
        Map<HttpMethod, Operation> ops = new LinkedHashMap<>();
        addOperationToMap(HttpMethod.GET, this.get, ops);
        addOperationToMap(HttpMethod.PUT, this.put, ops);
        addOperationToMap(HttpMethod.POST, this.post, ops);
        addOperationToMap(HttpMethod.DELETE, this.delete, ops);
        addOperationToMap(HttpMethod.OPTIONS, this.options, ops);
        addOperationToMap(HttpMethod.HEAD, this.head, ops);
        addOperationToMap(HttpMethod.PATCH, this.patch, ops);
        addOperationToMap(HttpMethod.TRACE, this.trace, ops);
        return ops;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.PathItem#getServers()
     */
    @Override
    public List<Server> getServers() {
        return this.servers;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.PathItem#setServers(java.util.List)
     */
    @Override
    public void setServers(List<Server> servers) {
        this.servers = servers;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.PathItem#servers(java.util.List)
     */
    @Override
    public PathItem servers(List<Server> servers) {
        this.servers = servers;
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.PathItem#addServer(org.eclipse.microprofile.openapi.models.servers.Server)
     */
    @Override
    public PathItem addServer(Server server) {
        if (this.servers == null) {
            this.servers = new ArrayList<>();
        }
        this.servers.add(server);
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.PathItem#getParameters()
     */
    @Override
    public List<Parameter> getParameters() {
        return this.parameters;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.PathItem#setParameters(java.util.List)
     */
    @Override
    public void setParameters(List<Parameter> parameters) {
        this.parameters = parameters;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.PathItem#parameters(java.util.List)
     */
    @Override
    public PathItem parameters(List<Parameter> parameters) {
        this.parameters = parameters;
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.PathItem#addParameter(org.eclipse.microprofile.openapi.models.parameters.Parameter)
     */
    @Override
    public PathItem addParameter(Parameter parameter) {
        if (this.parameters == null) {
            this.parameters = new ArrayList<>();
        }
        this.parameters.add(parameter);
        return this;
    }

    /**
     * Adds the given operation to the given list only if the operation is not null.
     * @param operation
     * @param operationList
     */
    private static void addOperationToList(Operation operation, List<Operation> operationList) {
        if (operation != null) {
            operationList.add(operation);
        }
    }

    /**
     * Adds the given operation to the given map only if the operation is not null.
     * @param method
     * @param operation
     * @param operationMap
     */
    private void addOperationToMap(HttpMethod method, Operation operation, Map<HttpMethod, Operation> operationMap) {
        if (operation != null) {
            operationMap.put(method, operation);
        }
    }

}