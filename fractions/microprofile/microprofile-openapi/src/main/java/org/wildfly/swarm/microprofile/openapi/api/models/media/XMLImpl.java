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

package org.wildfly.swarm.microprofile.openapi.api.models.media;

import org.eclipse.microprofile.openapi.models.media.XML;
import org.wildfly.swarm.microprofile.openapi.api.models.ExtensibleImpl;
import org.wildfly.swarm.microprofile.openapi.api.models.ModelImpl;

/**
 * An implementation of the {@link XML} OpenAPI model interface.
 */
public class XMLImpl extends ExtensibleImpl implements XML, ModelImpl {

    private String name;
    private String namespace;
    private String prefix;
    private Boolean attribute;
    private Boolean wrapped;

    /**
     * @see org.eclipse.microprofile.openapi.models.media.XML#getName()
     */
    @Override
    public String getName() {
        return this.name;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.XML#setName(java.lang.String)
     */
    @Override
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.XML#name(java.lang.String)
     */
    @Override
    public XML name(String name) {
        this.name = name;
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.XML#getNamespace()
     */
    @Override
    public String getNamespace() {
        return this.namespace;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.XML#setNamespace(java.lang.String)
     */
    @Override
    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.XML#namespace(java.lang.String)
     */
    @Override
    public XML namespace(String namespace) {
        this.namespace = namespace;
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.XML#getPrefix()
     */
    @Override
    public String getPrefix() {
        return this.prefix;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.XML#setPrefix(java.lang.String)
     */
    @Override
    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.XML#prefix(java.lang.String)
     */
    @Override
    public XML prefix(String prefix) {
        this.prefix = prefix;
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.XML#getAttribute()
     */
    @Override
    public Boolean getAttribute() {
        return this.attribute;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.XML#setAttribute(java.lang.Boolean)
     */
    @Override
    public void setAttribute(Boolean attribute) {
        this.attribute = attribute;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.XML#attribute(java.lang.Boolean)
     */
    @Override
    public XML attribute(Boolean attribute) {
        this.attribute = attribute;
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.XML#getWrapped()
     */
    @Override
    public Boolean getWrapped() {
        return this.wrapped;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.XML#setWrapped(java.lang.Boolean)
     */
    @Override
    public void setWrapped(Boolean wrapped) {
        this.wrapped = wrapped;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.XML#wrapped(java.lang.Boolean)
     */
    @Override
    public XML wrapped(Boolean wrapped) {
        this.wrapped = wrapped;
        return this;
    }

}