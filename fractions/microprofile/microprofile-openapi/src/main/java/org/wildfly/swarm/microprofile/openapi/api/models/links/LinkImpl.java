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

package org.wildfly.swarm.microprofile.openapi.api.models.links;

import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.microprofile.openapi.models.links.Link;
import org.eclipse.microprofile.openapi.models.servers.Server;
import org.wildfly.swarm.microprofile.openapi.api.models.ExtensibleImpl;
import org.wildfly.swarm.microprofile.openapi.api.models.ModelImpl;
import org.wildfly.swarm.microprofile.openapi.runtime.OpenApiConstants;

/**
 * An implementation of the {@link Link} OpenAPI model interface.
 */
public class LinkImpl extends ExtensibleImpl implements Link, ModelImpl {

    private String $ref;
    private String operationRef;
    private String operationId;
    private Map<String, Object> parameters;
    private Object requestBody;
    private String description;
    private Server server;

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
            ref = OpenApiConstants.REF_PREFIX_LINK + ref;
        }
        this.$ref = ref;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.Reference#ref(java.lang.String)
     */
    @Override
    public Link ref(String ref) {
        setRef(ref);
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.links.Link#getServer()
     */
    @Override
    public Server getServer() {
        return this.server;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.links.Link#setServer(org.eclipse.microprofile.openapi.models.servers.Server)
     */
    @Override
    public void setServer(Server server) {
        this.server = server;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.links.Link#server(org.eclipse.microprofile.openapi.models.servers.Server)
     */
    @Override
    public Link server(Server server) {
        this.server = server;
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.links.Link#getOperationRef()
     */
    @Override
    public String getOperationRef() {
        return this.operationRef;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.links.Link#setOperationRef(java.lang.String)
     */
    @Override
    public void setOperationRef(String operationRef) {
        this.operationRef = operationRef;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.links.Link#operationRef(java.lang.String)
     */
    @Override
    public Link operationRef(String operationRef) {
        this.operationRef = operationRef;
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.links.Link#getRequestBody()
     */
    @Override
    public Object getRequestBody() {
        return this.requestBody;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.links.Link#setRequestBody(java.lang.Object)
     */
    @Override
    public void setRequestBody(Object requestBody) {
        this.requestBody = requestBody;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.links.Link#requestBody(java.lang.Object)
     */
    @Override
    public Link requestBody(Object requestBody) {
        this.requestBody = requestBody;
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.links.Link#getOperationId()
     */
    @Override
    public String getOperationId() {
        return this.operationId;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.links.Link#setOperationId(java.lang.String)
     */
    @Override
    public void setOperationId(String operationId) {
        this.operationId = operationId;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.links.Link#operationId(java.lang.String)
     */
    @Override
    public Link operationId(String operationId) {
        this.operationId = operationId;
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.links.Link#getParameters()
     */
    @Override
    public Map<String, Object> getParameters() {
        return this.parameters;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.links.Link#setParameters(java.util.Map)
     */
    @Override
    public void setParameters(Map<String, Object> parameters) {
        this.parameters = parameters;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.links.Link#parameters(java.util.Map)
     */
    @Override
    public Link parameters(Map<String, Object> parameters) {
        this.parameters = parameters;
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.links.Link#addParameter(java.lang.String, java.lang.Object)
     */
    @Override
    public Link addParameter(String name, Object parameter) {
        if (this.parameters == null) {
            this.parameters = new LinkedHashMap<>();
        }
        this.parameters.put(name, parameter);
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.links.Link#getDescription()
     */
    @Override
    public String getDescription() {
        return this.description;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.links.Link#setDescription(java.lang.String)
     */
    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.links.Link#description(java.lang.String)
     */
    @Override
    public Link description(String description) {
        this.description = description;
        return this;
    }

}