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

package org.jboss.unimbus.openapi.impl.api.models.media;

import org.eclipse.microprofile.openapi.models.media.XML;
import org.jboss.unimbus.openapi.impl.api.models.ExtensibleImpl;
import org.jboss.unimbus.openapi.impl.api.models.ModelImpl;

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
     * @see XML#getName()
     */
    @Override
    public String getName() {
        return this.name;
    }

    /**
     * @see XML#setName(String)
     */
    @Override
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @see XML#name(String)
     */
    @Override
    public XML name(String name) {
        this.name = name;
        return this;
    }

    /**
     * @see XML#getNamespace()
     */
    @Override
    public String getNamespace() {
        return this.namespace;
    }

    /**
     * @see XML#setNamespace(String)
     */
    @Override
    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    /**
     * @see XML#namespace(String)
     */
    @Override
    public XML namespace(String namespace) {
        this.namespace = namespace;
        return this;
    }

    /**
     * @see XML#getPrefix()
     */
    @Override
    public String getPrefix() {
        return this.prefix;
    }

    /**
     * @see XML#setPrefix(String)
     */
    @Override
    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    /**
     * @see XML#prefix(String)
     */
    @Override
    public XML prefix(String prefix) {
        this.prefix = prefix;
        return this;
    }

    /**
     * @see XML#getAttribute()
     */
    @Override
    public Boolean getAttribute() {
        return this.attribute;
    }

    /**
     * @see XML#setAttribute(Boolean)
     */
    @Override
    public void setAttribute(Boolean attribute) {
        this.attribute = attribute;
    }

    /**
     * @see XML#attribute(Boolean)
     */
    @Override
    public XML attribute(Boolean attribute) {
        this.attribute = attribute;
        return this;
    }

    /**
     * @see XML#getWrapped()
     */
    @Override
    public Boolean getWrapped() {
        return this.wrapped;
    }

    /**
     * @see XML#setWrapped(Boolean)
     */
    @Override
    public void setWrapped(Boolean wrapped) {
        this.wrapped = wrapped;
    }

    /**
     * @see XML#wrapped(Boolean)
     */
    @Override
    public XML wrapped(Boolean wrapped) {
        this.wrapped = wrapped;
        return this;
    }

}