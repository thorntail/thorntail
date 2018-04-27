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

package io.thorntail.openapi.impl.api.models.links;

import java.util.LinkedHashMap;
import java.util.Map;

import io.thorntail.openapi.impl.api.models.ExtensibleImpl;
import io.thorntail.openapi.impl.api.models.ModelImpl;
import org.eclipse.microprofile.openapi.models.links.Link;
import org.eclipse.microprofile.openapi.models.servers.Server;
import io.thorntail.openapi.impl.OpenApiConstants;

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
     * @see org.eclipse.microprofile.openapi.models.Reference#setRef(String)
     */
    @Override
    public void setRef(String ref) {
        if (ref != null && !ref.contains("/")) {
            ref = OpenApiConstants.REF_PREFIX_LINK + ref;
        }
        this.$ref = ref;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.Reference#ref(String)
     */
    @Override
    public Link ref(String ref) {
        setRef(ref);
        return this;
    }

    /**
     * @see Link#getServer()
     */
    @Override
    public Server getServer() {
        return this.server;
    }

    /**
     * @see Link#setServer(Server)
     */
    @Override
    public void setServer(Server server) {
        this.server = server;
    }

    /**
     * @see Link#server(Server)
     */
    @Override
    public Link server(Server server) {
        this.server = server;
        return this;
    }

    /**
     * @see Link#getOperationRef()
     */
    @Override
    public String getOperationRef() {
        return this.operationRef;
    }

    /**
     * @see Link#setOperationRef(String)
     */
    @Override
    public void setOperationRef(String operationRef) {
        this.operationRef = operationRef;
    }

    /**
     * @see Link#operationRef(String)
     */
    @Override
    public Link operationRef(String operationRef) {
        this.operationRef = operationRef;
        return this;
    }

    /**
     * @see Link#getRequestBody()
     */
    @Override
    public Object getRequestBody() {
        return this.requestBody;
    }

    /**
     * @see Link#setRequestBody(Object)
     */
    @Override
    public void setRequestBody(Object requestBody) {
        this.requestBody = requestBody;
    }

    /**
     * @see Link#requestBody(Object)
     */
    @Override
    public Link requestBody(Object requestBody) {
        this.requestBody = requestBody;
        return this;
    }

    /**
     * @see Link#getOperationId()
     */
    @Override
    public String getOperationId() {
        return this.operationId;
    }

    /**
     * @see Link#setOperationId(String)
     */
    @Override
    public void setOperationId(String operationId) {
        this.operationId = operationId;
    }

    /**
     * @see Link#operationId(String)
     */
    @Override
    public Link operationId(String operationId) {
        this.operationId = operationId;
        return this;
    }

    /**
     * @see Link#getParameters()
     */
    @Override
    public Map<String, Object> getParameters() {
        return this.parameters;
    }

    /**
     * @see Link#setParameters(Map)
     */
    @Override
    public void setParameters(Map<String, Object> parameters) {
        this.parameters = parameters;
    }

    /**
     * @see Link#parameters(Map)
     */
    @Override
    public Link parameters(Map<String, Object> parameters) {
        this.parameters = parameters;
        return this;
    }

    /**
     * @see Link#addParameter(String, Object)
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
     * @see Link#getDescription()
     */
    @Override
    public String getDescription() {
        return this.description;
    }

    /**
     * @see Link#setDescription(String)
     */
    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @see Link#description(String)
     */
    @Override
    public Link description(String description) {
        this.description = description;
        return this;
    }

}