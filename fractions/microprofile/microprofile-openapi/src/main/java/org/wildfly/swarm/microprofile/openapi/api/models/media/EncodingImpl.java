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

import java.util.Map;

import org.eclipse.microprofile.openapi.models.headers.Header;
import org.eclipse.microprofile.openapi.models.media.Encoding;
import org.wildfly.swarm.microprofile.openapi.api.models.ExtensibleImpl;
import org.wildfly.swarm.microprofile.openapi.api.models.ModelImpl;

/**
 * An implementation of the {@link Encoding} OpenAPI model interface.
 */
public class EncodingImpl extends ExtensibleImpl implements Encoding, ModelImpl {

    private String contentType;
    private Map<String, Header> headers;
    private Style style;
    private Boolean explode;
    private Boolean allowReserved;

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Encoding#contentType(java.lang.String)
     */
    @Override
    public Encoding contentType(String contentType) {
        this.contentType = contentType;
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Encoding#getContentType()
     */
    @Override
    public String getContentType() {
        return this.contentType;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Encoding#setContentType(java.lang.String)
     */
    @Override
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Encoding#headers(java.util.Map)
     */
    @Override
    public Encoding headers(Map<String, Header> headers) {
        this.headers = headers;
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Encoding#getHeaders()
     */
    @Override
    public Map<String, Header> getHeaders() {
        return this.headers;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Encoding#setHeaders(java.util.Map)
     */
    @Override
    public void setHeaders(Map<String, Header> headers) {
        this.headers = headers;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Encoding#style(org.eclipse.microprofile.openapi.models.media.Encoding.Style)
     */
    @Override
    public Encoding style(Style style) {
        this.style = style;
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Encoding#getStyle()
     */
    @Override
    public Style getStyle() {
        return this.style;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Encoding#setStyle(org.eclipse.microprofile.openapi.models.media.Encoding.Style)
     */
    @Override
    public void setStyle(Style style) {
        this.style = style;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Encoding#explode(java.lang.Boolean)
     */
    @Override
    public Encoding explode(Boolean explode) {
        this.explode = explode;
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Encoding#getExplode()
     */
    @Override
    public Boolean getExplode() {
        return this.explode;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Encoding#setExplode(java.lang.Boolean)
     */
    @Override
    public void setExplode(Boolean explode) {
        this.explode = explode;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Encoding#allowReserved(java.lang.Boolean)
     */
    @Override
    public Encoding allowReserved(Boolean allowReserved) {
        this.allowReserved = allowReserved;
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Encoding#getAllowReserved()
     */
    @Override
    public Boolean getAllowReserved() {
        return this.allowReserved;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Encoding#setAllowReserved(java.lang.Boolean)
     */
    @Override
    public void setAllowReserved(Boolean allowReserved) {
        this.allowReserved = allowReserved;
    }

}