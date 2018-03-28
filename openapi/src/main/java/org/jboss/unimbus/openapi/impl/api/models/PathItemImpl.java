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
     * @see org.eclipse.microprofile.openapi.models.Reference#setRef(String)
     */
    @Override
    public void setRef(String ref) {
        this.$ref = ref;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.Reference#ref(String)
     */
    @Override
    public PathItem ref(String ref) {
        this.$ref = ref;
        return this;
    }

    /**
     * @see PathItem#getSummary()
     */
    @Override
    public String getSummary() {
        return this.summary;
    }

    /**
     * @see PathItem#setSummary(String)
     */
    @Override
    public void setSummary(String summary) {
        this.summary = summary;
    }

    /**
     * @see PathItem#summary(String)
     */
    @Override
    public PathItem summary(String summary) {
        this.summary = summary;
        return this;
    }

    /**
     * @see PathItem#getDescription()
     */
    @Override
    public String getDescription() {
        return this.description;
    }

    /**
     * @see PathItem#setDescription(String)
     */
    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @see PathItem#description(String)
     */
    @Override
    public PathItem description(String description) {
        this.description = description;
        return this;
    }

    /**
     * @see PathItem#getGET()
     */
    @Override
    public Operation getGET() {
        return this.get;
    }

    /**
     * @see PathItem#setGET(Operation)
     */
    @Override
    public void setGET(Operation get) {
        this.get = get;
    }

    /**
     * @see PathItem#GET(Operation)
     */
    @Override
    public PathItem GET(Operation get) {
        this.get = get;
        return this;
    }

    /**
     * @see PathItem#getPUT()
     */
    @Override
    public Operation getPUT() {
        return this.put;
    }

    /**
     * @see PathItem#setPUT(Operation)
     */
    @Override
    public void setPUT(Operation put) {
        this.put = put;
    }

    /**
     * @see PathItem#PUT(Operation)
     */
    @Override
    public PathItem PUT(Operation put) {
        this.put = put;
        return this;
    }

    /**
     * @see PathItem#getPOST()
     */
    @Override
    public Operation getPOST() {
        return this.post;
    }

    /**
     * @see PathItem#setPOST(Operation)
     */
    @Override
    public void setPOST(Operation post) {
        this.post = post;
    }

    /**
     * @see PathItem#POST(Operation)
     */
    @Override
    public PathItem POST(Operation post) {
        this.post = post;
        return this;
    }

    /**
     * @see PathItem#getDELETE()
     */
    @Override
    public Operation getDELETE() {
        return this.delete;
    }

    /**
     * @see PathItem#setDELETE(Operation)
     */
    @Override
    public void setDELETE(Operation delete) {
        this.delete = delete;
    }

    /**
     * @see PathItem#DELETE(Operation)
     */
    @Override
    public PathItem DELETE(Operation delete) {
        this.delete = delete;
        return this;
    }

    /**
     * @see PathItem#getOPTIONS()
     */
    @Override
    public Operation getOPTIONS() {
        return this.options;
    }

    /**
     * @see PathItem#setOPTIONS(Operation)
     */
    @Override
    public void setOPTIONS(Operation options) {
        this.options = options;
    }

    /**
     * @see PathItem#OPTIONS(Operation)
     */
    @Override
    public PathItem OPTIONS(Operation options) {
        this.options = options;
        return this;
    }

    /**
     * @see PathItem#getHEAD()
     */
    @Override
    public Operation getHEAD() {
        return this.head;
    }

    /**
     * @see PathItem#setHEAD(Operation)
     */
    @Override
    public void setHEAD(Operation head) {
        this.head = head;
    }

    /**
     * @see PathItem#HEAD(Operation)
     */
    @Override
    public PathItem HEAD(Operation head) {
        this.head = head;
        return this;
    }

    /**
     * @see PathItem#getPATCH()
     */
    @Override
    public Operation getPATCH() {
        return this.patch;
    }

    /**
     * @see PathItem#setPATCH(Operation)
     */
    @Override
    public void setPATCH(Operation patch) {
        this.patch = patch;
    }

    /**
     * @see PathItem#PATCH(Operation)
     */
    @Override
    public PathItem PATCH(Operation patch) {
        this.patch = patch;
        return this;
    }

    /**
     * @see PathItem#getTRACE()
     */
    @Override
    public Operation getTRACE() {
        return this.trace;
    }

    /**
     * @see PathItem#setTRACE(Operation)
     */
    @Override
    public void setTRACE(Operation trace) {
        this.trace = trace;
    }

    /**
     * @see PathItem#TRACE(Operation)
     */
    @Override
    public PathItem TRACE(Operation trace) {
        this.trace = trace;
        return this;
    }

    /**
     * @see PathItem#readOperations()
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
     * @see PathItem#readOperationsMap()
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
     * @see PathItem#getServers()
     */
    @Override
    public List<Server> getServers() {
        return this.servers;
    }

    /**
     * @see PathItem#setServers(List)
     */
    @Override
    public void setServers(List<Server> servers) {
        this.servers = servers;
    }

    /**
     * @see PathItem#servers(List)
     */
    @Override
    public PathItem servers(List<Server> servers) {
        this.servers = servers;
        return this;
    }

    /**
     * @see PathItem#addServer(Server)
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
     * @see PathItem#getParameters()
     */
    @Override
    public List<Parameter> getParameters() {
        return this.parameters;
    }

    /**
     * @see PathItem#setParameters(List)
     */
    @Override
    public void setParameters(List<Parameter> parameters) {
        this.parameters = parameters;
    }

    /**
     * @see PathItem#parameters(List)
     */
    @Override
    public PathItem parameters(List<Parameter> parameters) {
        this.parameters = parameters;
        return this;
    }

    /**
     * @see PathItem#addParameter(Parameter)
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
     *
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
     *
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