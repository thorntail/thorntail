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

package io.thorntail.openapi.impl.api.models.headers;

import java.util.LinkedHashMap;
import java.util.Map;

import io.thorntail.openapi.impl.api.models.ExtensibleImpl;
import org.eclipse.microprofile.openapi.models.examples.Example;
import org.eclipse.microprofile.openapi.models.headers.Header;
import org.eclipse.microprofile.openapi.models.media.Content;
import org.eclipse.microprofile.openapi.models.media.Schema;
import io.thorntail.openapi.impl.api.models.ModelImpl;
import io.thorntail.openapi.impl.OpenApiConstants;

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
     * @see org.eclipse.microprofile.openapi.models.Reference#setRef(String)
     */
    @Override
    public void setRef(String ref) {
        if (ref != null && !ref.contains("/")) {
            ref = OpenApiConstants.REF_PREFIX_HEADER + ref;
        }
        this.$ref = ref;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.Reference#ref(String)
     */
    @Override
    public Header ref(String ref) {
        setRef(ref);
        return this;
    }

    /**
     * @see Header#getDescription()
     */
    @Override
    public String getDescription() {
        return this.description;
    }

    /**
     * @see Header#setDescription(String)
     */
    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @see Header#description(String)
     */
    @Override
    public Header description(String description) {
        this.description = description;
        return this;
    }

    /**
     * @see Header#getRequired()
     */
    @Override
    public Boolean getRequired() {
        return this.required;
    }

    /**
     * @see Header#setRequired(Boolean)
     */
    @Override
    public void setRequired(Boolean required) {
        this.required = required;
    }

    /**
     * @see Header#required(Boolean)
     */
    @Override
    public Header required(Boolean required) {
        this.required = required;
        return this;
    }

    /**
     * @see Header#getDeprecated()
     */
    @Override
    public Boolean getDeprecated() {
        return this.deprecated;
    }

    /**
     * @see Header#setDeprecated(Boolean)
     */
    @Override
    public void setDeprecated(Boolean deprecated) {
        this.deprecated = deprecated;
    }

    /**
     * @see Header#deprecated(Boolean)
     */
    @Override
    public Header deprecated(Boolean deprecated) {
        this.deprecated = deprecated;
        return this;
    }

    /**
     * @see Header#getAllowEmptyValue()
     */
    @Override
    public Boolean getAllowEmptyValue() {
        return this.allowEmptyValue;
    }

    /**
     * @see Header#setAllowEmptyValue(Boolean)
     */
    @Override
    public void setAllowEmptyValue(Boolean allowEmptyValue) {
        this.allowEmptyValue = allowEmptyValue;
    }

    /**
     * @see Header#allowEmptyValue(Boolean)
     */
    @Override
    public Header allowEmptyValue(Boolean allowEmptyValue) {
        this.allowEmptyValue = allowEmptyValue;
        return this;
    }

    /**
     * @see Header#getStyle()
     */
    @Override
    public Style getStyle() {
        return this.style;
    }

    /**
     * @see Header#setStyle(Style)
     */
    @Override
    public void setStyle(Style style) {
        this.style = style;
    }

    /**
     * @see Header#style(Style)
     */
    @Override
    public Header style(Style style) {
        this.style = style;
        return this;
    }

    /**
     * @see Header#getExplode()
     */
    @Override
    public Boolean getExplode() {
        return this.explode;
    }

    /**
     * @see Header#setExplode(Boolean)
     */
    @Override
    public void setExplode(Boolean explode) {
        this.explode = explode;
    }

    /**
     * @see Header#explode(Boolean)
     */
    @Override
    public Header explode(Boolean explode) {
        this.explode = explode;
        return this;
    }

    /**
     * @see Header#getSchema()
     */
    @Override
    public Schema getSchema() {
        return this.schema;
    }

    /**
     * @see Header#setSchema(Schema)
     */
    @Override
    public void setSchema(Schema schema) {
        this.schema = schema;
    }

    /**
     * @see Header#schema(Schema)
     */
    @Override
    public Header schema(Schema schema) {
        this.schema = schema;
        return this;
    }

    /**
     * @see Header#getExamples()
     */
    @Override
    public Map<String, Example> getExamples() {
        return this.examples;
    }

    /**
     * @see Header#setExamples(Map)
     */
    @Override
    public void setExamples(Map<String, Example> examples) {
        this.examples = examples;
    }

    /**
     * @see Header#examples(Map)
     */
    @Override
    public Header examples(Map<String, Example> examples) {
        this.examples = examples;
        return this;
    }

    /**
     * @see Header#addExample(String, Example)
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
     * @see Header#getExample()
     */
    @Override
    public Object getExample() {
        return this.example;
    }

    /**
     * @see Header#setExample(Object)
     */
    @Override
    public void setExample(Object example) {
        this.example = example;
    }

    /**
     * @see Header#example(Object)
     */
    @Override
    public Header example(Object example) {
        this.example = example;
        return this;
    }

    /**
     * @see Header#getContent()
     */
    @Override
    public Content getContent() {
        return this.content;
    }

    /**
     * @see Header#setContent(Content)
     */
    @Override
    public void setContent(Content content) {
        this.content = content;
    }

    /**
     * @see Header#content(Content)
     */
    @Override
    public Header content(Content content) {
        this.content = content;
        return this;
    }

}