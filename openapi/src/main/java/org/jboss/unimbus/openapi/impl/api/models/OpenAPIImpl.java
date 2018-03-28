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

import org.eclipse.microprofile.openapi.models.Components;
import org.eclipse.microprofile.openapi.models.ExternalDocumentation;
import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.eclipse.microprofile.openapi.models.PathItem;
import org.eclipse.microprofile.openapi.models.Paths;
import org.eclipse.microprofile.openapi.models.info.Info;
import org.eclipse.microprofile.openapi.models.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.models.servers.Server;
import org.eclipse.microprofile.openapi.models.tags.Tag;

/**
 * An implementation of the {@link OpenAPI} OpenAPI model interface.
 */
public class OpenAPIImpl extends ExtensibleImpl implements OpenAPI, ModelImpl {

    private String openapi;

    private Info info;

    private ExternalDocumentation externalDocs;

    private List<Server> servers;

    private List<SecurityRequirement> security;

    private List<Tag> tags;

    private Paths paths;

    private Components components;

    /**
     * @see OpenAPI#getOpenapi()
     */
    @Override
    public String getOpenapi() {
        return this.openapi;
    }

    /**
     * @see OpenAPI#setOpenapi(String)
     */
    @Override
    public void setOpenapi(String openapi) {
        this.openapi = openapi;
    }

    /**
     * @see OpenAPI#openapi(String)
     */
    @Override
    public OpenAPI openapi(String openapi) {
        this.openapi = openapi;
        return this;
    }

    /**
     * @see OpenAPI#getInfo()
     */
    @Override
    public Info getInfo() {
        return this.info;
    }

    /**
     * @see OpenAPI#setInfo(Info)
     */
    @Override
    public void setInfo(Info info) {
        this.info = info;
    }

    /**
     * @see OpenAPI#info(Info)
     */
    @Override
    public OpenAPI info(Info info) {
        this.info = info;
        return this;
    }

    /**
     * @see OpenAPI#getExternalDocs()
     */
    @Override
    public ExternalDocumentation getExternalDocs() {
        return this.externalDocs;
    }

    /**
     * @see OpenAPI#setExternalDocs(ExternalDocumentation)
     */
    @Override
    public void setExternalDocs(ExternalDocumentation externalDocs) {
        this.externalDocs = externalDocs;
    }

    /**
     * @see OpenAPI#externalDocs(ExternalDocumentation)
     */
    @Override
    public OpenAPI externalDocs(ExternalDocumentation externalDocs) {
        this.externalDocs = externalDocs;
        return this;
    }

    /**
     * @see OpenAPI#getServers()
     */
    @Override
    public List<Server> getServers() {
        return this.servers;
    }

    /**
     * @see OpenAPI#setServers(List)
     */
    @Override
    public void setServers(List<Server> servers) {
        this.servers = servers;
    }

    /**
     * @see OpenAPI#servers(List)
     */
    @Override
    public OpenAPI servers(List<Server> servers) {
        this.servers = servers;
        return this;
    }

    /**
     * @see OpenAPI#addServer(Server)
     */
    @Override
    public OpenAPI addServer(Server server) {
        if (this.servers == null) {
            this.servers = new ArrayList<>();
        }
        this.servers.add(server);
        return this;
    }

    /**
     * @see OpenAPI#getSecurity()
     */
    @Override
    public List<SecurityRequirement> getSecurity() {
        return this.security;
    }

    /**
     * @see OpenAPI#setSecurity(List)
     */
    @Override
    public void setSecurity(List<SecurityRequirement> security) {
        this.security = security;
    }

    /**
     * @see OpenAPI#security(List)
     */
    @Override
    public OpenAPI security(List<SecurityRequirement> security) {
        this.security = security;
        return this;
    }

    /**
     * @see OpenAPI#addSecurityRequirement(SecurityRequirement)
     */
    @Override
    public OpenAPI addSecurityRequirement(SecurityRequirement securityRequirement) {
        if (this.security == null) {
            this.security = new ArrayList<>();
        }
        this.security.add(securityRequirement);
        return this;
    }

    /**
     * @see OpenAPI#getTags()
     */
    @Override
    public List<Tag> getTags() {
        return this.tags;
    }

    /**
     * @see OpenAPI#setTags(List)
     */
    @Override
    public void setTags(List<Tag> tags) {
        this.tags = tags;
    }

    /**
     * @see OpenAPI#tags(List)
     */
    @Override
    public OpenAPI tags(List<Tag> tags) {
        this.tags = tags;
        return this;
    }

    /**
     * @see OpenAPI#addTag(Tag)
     */
    @Override
    public OpenAPI addTag(Tag tag) {
        if (this.tags == null) {
            this.tags = new ArrayList<>();
        }
        if (!this.hasTag(tag.getName())) {
            this.tags.add(tag);
        }
        return this;
    }

    /**
     * Returns true if the tag already exists in the OpenAPI document.
     *
     * @param name
     */
    private boolean hasTag(String name) {
        // TODO Auto-generated method stub
        return false;
    }

    /**
     * @see OpenAPI#getPaths()
     */
    @Override
    public Paths getPaths() {
        return this.paths;
    }

    /**
     * @see OpenAPI#setPaths(Paths)
     */
    @Override
    public void setPaths(Paths paths) {
        this.paths = paths;
    }

    /**
     * @see OpenAPI#paths(Paths)
     */
    @Override
    public OpenAPI paths(Paths paths) {
        this.paths = paths;
        return this;
    }

    /**
     * @see OpenAPI#path(String, PathItem)
     */
    @Override
    public OpenAPI path(String name, PathItem path) {
        if (this.paths == null) {
            this.paths = new PathsImpl();
        }
        this.paths.addPathItem(name, path);
        return this;
    }

    /**
     * @see OpenAPI#getComponents()
     */
    @Override
    public Components getComponents() {
        return this.components;
    }

    /**
     * @see OpenAPI#setComponents(Components)
     */
    @Override
    public void setComponents(Components components) {
        this.components = components;
    }

    /**
     * @see OpenAPI#components(Components)
     */
    @Override
    public OpenAPI components(Components components) {
        this.components = components;
        return this;
    }
}