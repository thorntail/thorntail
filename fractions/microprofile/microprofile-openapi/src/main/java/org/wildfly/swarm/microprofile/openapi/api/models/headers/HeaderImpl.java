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

package org.wildfly.swarm.microprofile.openapi.api.models.headers;

import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.microprofile.openapi.models.examples.Example;
import org.eclipse.microprofile.openapi.models.headers.Header;
import org.eclipse.microprofile.openapi.models.media.Content;
import org.eclipse.microprofile.openapi.models.media.Schema;
import org.wildfly.swarm.microprofile.openapi.api.models.ExtensibleImpl;
import org.wildfly.swarm.microprofile.openapi.api.models.ModelImpl;
import org.wildfly.swarm.microprofile.openapi.runtime.OpenApiConstants;

/**
 * An implementation of the {@link Header} OpenAPI model interface.
 */
public class HeaderImpl extends ExtensibleImpl implements Header, ModelImpl {

    private String $ref;
    private String description;
    private Boolean required;
    private Boolean deprecated;
    private Boolean allowEmptyValue;
    private Style style = Style.SIMPLE;
    private Boolean explode;
    private Schema schema;
    private Object example;
    private Map<String, Example> examples;
    private Content content;

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
            ref = OpenApiConstants.REF_PREFIX_HEADER + ref;
        }
        this.$ref = ref;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.Reference#ref(java.lang.String)
     */
    @Override
    public Header ref(String ref) {
        setRef(ref);
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.headers.Header#getDescription()
     */
    @Override
    public String getDescription() {
        return this.description;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.headers.Header#setDescription(java.lang.String)
     */
    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.headers.Header#description(java.lang.String)
     */
    @Override
    public Header description(String description) {
        this.description = description;
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.headers.Header#getRequired()
     */
    @Override
    public Boolean getRequired() {
        return this.required;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.headers.Header#setRequired(java.lang.Boolean)
     */
    @Override
    public void setRequired(Boolean required) {
        this.required = required;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.headers.Header#required(java.lang.Boolean)
     */
    @Override
    public Header required(Boolean required) {
        this.required = required;
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.headers.Header#getDeprecated()
     */
    @Override
    public Boolean getDeprecated() {
        return this.deprecated;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.headers.Header#setDeprecated(java.lang.Boolean)
     */
    @Override
    public void setDeprecated(Boolean deprecated) {
        this.deprecated = deprecated;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.headers.Header#deprecated(java.lang.Boolean)
     */
    @Override
    public Header deprecated(Boolean deprecated) {
        this.deprecated = deprecated;
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.headers.Header#getAllowEmptyValue()
     */
    @Override
    public Boolean getAllowEmptyValue() {
        return this.allowEmptyValue;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.headers.Header#setAllowEmptyValue(java.lang.Boolean)
     */
    @Override
    public void setAllowEmptyValue(Boolean allowEmptyValue) {
        this.allowEmptyValue = allowEmptyValue;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.headers.Header#allowEmptyValue(java.lang.Boolean)
     */
    @Override
    public Header allowEmptyValue(Boolean allowEmptyValue) {
        this.allowEmptyValue = allowEmptyValue;
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.headers.Header#getStyle()
     */
    @Override
    public Style getStyle() {
        return this.style;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.headers.Header#setStyle(org.eclipse.microprofile.openapi.models.headers.Header.Style)
     */
    @Override
    public void setStyle(Style style) {
        this.style = style;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.headers.Header#style(org.eclipse.microprofile.openapi.models.headers.Header.Style)
     */
    @Override
    public Header style(Style style) {
        this.style = style;
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.headers.Header#getExplode()
     */
    @Override
    public Boolean getExplode() {
        return this.explode;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.headers.Header#setExplode(java.lang.Boolean)
     */
    @Override
    public void setExplode(Boolean explode) {
        this.explode = explode;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.headers.Header#explode(java.lang.Boolean)
     */
    @Override
    public Header explode(Boolean explode) {
        this.explode = explode;
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.headers.Header#getSchema()
     */
    @Override
    public Schema getSchema() {
        return this.schema;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.headers.Header#setSchema(org.eclipse.microprofile.openapi.models.media.Schema)
     */
    @Override
    public void setSchema(Schema schema) {
        this.schema = schema;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.headers.Header#schema(org.eclipse.microprofile.openapi.models.media.Schema)
     */
    @Override
    public Header schema(Schema schema) {
        this.schema = schema;
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.headers.Header#getExamples()
     */
    @Override
    public Map<String, Example> getExamples() {
        return this.examples;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.headers.Header#setExamples(java.util.Map)
     */
    @Override
    public void setExamples(Map<String, Example> examples) {
        this.examples = examples;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.headers.Header#examples(java.util.Map)
     */
    @Override
    public Header examples(Map<String, Example> examples) {
        this.examples = examples;
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.headers.Header#addExample(java.lang.String, org.eclipse.microprofile.openapi.models.examples.Example)
     */
    @Override
    public Header addExample(String key, Example example) {
        if (this.examples == null) {
            this.examples = new LinkedHashMap<>();
        }
        this.examples.put(key, example);
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.headers.Header#getExample()
     */
    @Override
    public Object getExample() {
        return this.example;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.headers.Header#setExample(java.lang.Object)
     */
    @Override
    public void setExample(Object example) {
        this.example = example;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.headers.Header#example(java.lang.Object)
     */
    @Override
    public Header example(Object example) {
        this.example = example;
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.headers.Header#getContent()
     */
    @Override
    public Content getContent() {
        return this.content;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.headers.Header#setContent(org.eclipse.microprofile.openapi.models.media.Content)
     */
    @Override
    public void setContent(Content content) {
        this.content = content;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.headers.Header#content(org.eclipse.microprofile.openapi.models.media.Content)
     */
    @Override
    public Header content(Content content) {
        this.content = content;
        return this;
    }

}