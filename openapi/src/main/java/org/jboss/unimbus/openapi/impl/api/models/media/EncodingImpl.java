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

import java.util.Map;

import org.eclipse.microprofile.openapi.models.headers.Header;
import org.eclipse.microprofile.openapi.models.media.Encoding;
import org.jboss.unimbus.openapi.impl.api.models.ExtensibleImpl;
import org.jboss.unimbus.openapi.impl.api.models.ModelImpl;

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
     * @see Encoding#contentType(String)
     */
    @Override
    public Encoding contentType(String contentType) {
        this.contentType = contentType;
        return this;
    }

    /**
     * @see Encoding#getContentType()
     */
    @Override
    public String getContentType() {
        return this.contentType;
    }

    /**
     * @see Encoding#setContentType(String)
     */
    @Override
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    /**
     * @see Encoding#headers(Map)
     */
    @Override
    public Encoding headers(Map<String, Header> headers) {
        this.headers = headers;
        return this;
    }

    /**
     * @see Encoding#getHeaders()
     */
    @Override
    public Map<String, Header> getHeaders() {
        return this.headers;
    }

    /**
     * @see Encoding#setHeaders(Map)
     */
    @Override
    public void setHeaders(Map<String, Header> headers) {
        this.headers = headers;
    }

    /**
     * @see Encoding#style(Style)
     */
    @Override
    public Encoding style(Style style) {
        this.style = style;
        return this;
    }

    /**
     * @see Encoding#getStyle()
     */
    @Override
    public Style getStyle() {
        return this.style;
    }

    /**
     * @see Encoding#setStyle(Style)
     */
    @Override
    public void setStyle(Style style) {
        this.style = style;
    }

    /**
     * @see Encoding#explode(Boolean)
     */
    @Override
    public Encoding explode(Boolean explode) {
        this.explode = explode;
        return this;
    }

    /**
     * @see Encoding#getExplode()
     */
    @Override
    public Boolean getExplode() {
        return this.explode;
    }

    /**
     * @see Encoding#setExplode(Boolean)
     */
    @Override
    public void setExplode(Boolean explode) {
        this.explode = explode;
    }

    /**
     * @see Encoding#allowReserved(Boolean)
     */
    @Override
    public Encoding allowReserved(Boolean allowReserved) {
        this.allowReserved = allowReserved;
        return this;
    }

    /**
     * @see Encoding#getAllowReserved()
     */
    @Override
    public Boolean getAllowReserved() {
        return this.allowReserved;
    }

    /**
     * @see Encoding#setAllowReserved(Boolean)
     */
    @Override
    public void setAllowReserved(Boolean allowReserved) {
        this.allowReserved = allowReserved;
    }

}