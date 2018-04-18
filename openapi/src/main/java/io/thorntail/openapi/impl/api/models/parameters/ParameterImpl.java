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

package io.thorntail.openapi.impl.api.models.parameters;

import java.util.LinkedHashMap;
import java.util.Map;

import io.thorntail.openapi.impl.OpenApiConstants;
import io.thorntail.openapi.impl.api.models.ExtensibleImpl;
import io.thorntail.openapi.impl.api.models.ModelImpl;
import org.eclipse.microprofile.openapi.models.examples.Example;
import org.eclipse.microprofile.openapi.models.media.Content;
import org.eclipse.microprofile.openapi.models.media.Schema;
import org.eclipse.microprofile.openapi.models.parameters.Parameter;

/**
 * An implementation of the {@link Parameter} OpenAPI model interface.
 */
public class ParameterImpl extends ExtensibleImpl implements Parameter, ModelImpl {

    private String $ref;

    private String name;

    private In in;

    private String description;

    private Boolean required;

    private Schema schema;

    private Boolean allowEmptyValue;

    private Boolean deprecated;

    private Style style;

    private Boolean explode;

    private Boolean allowReserved;

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
            ref = OpenApiConstants.REF_PREFIX_PARAMETER + ref;
        }
        this.$ref = ref;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.Reference#ref(String)
     */
    @Override
    public Parameter ref(String ref) {
        setRef(ref);
        return this;
    }

    /**
     * @see Parameter#getName()
     */
    @Override
    public String getName() {
        return this.name;
    }

    /**
     * @see Parameter#setName(String)
     */
    @Override
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @see Parameter#name(String)
     */
    @Override
    public Parameter name(String name) {
        this.name = name;
        return this;
    }

    /**
     * @see Parameter#getDescription()
     */
    @Override
    public String getDescription() {
        return this.description;
    }

    /**
     * @see Parameter#setDescription(String)
     */
    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @see Parameter#description(String)
     */
    @Override
    public Parameter description(String description) {
        this.description = description;
        return this;
    }

    /**
     * @see Parameter#getRequired()
     */
    @Override
    public Boolean getRequired() {
        return this.required;
    }

    /**
     * @see Parameter#setRequired(Boolean)
     */
    @Override
    public void setRequired(Boolean required) {
        this.required = required;
    }

    /**
     * @see Parameter#required(Boolean)
     */
    @Override
    public Parameter required(Boolean required) {
        this.required = required;
        return this;
    }

    /**
     * @see Parameter#getDeprecated()
     */
    @Override
    public Boolean getDeprecated() {
        return this.deprecated;
    }

    /**
     * @see Parameter#setDeprecated(Boolean)
     */
    @Override
    public void setDeprecated(Boolean deprecated) {
        this.deprecated = deprecated;
    }

    /**
     * @see Parameter#deprecated(Boolean)
     */
    @Override
    public Parameter deprecated(Boolean deprecated) {
        this.deprecated = deprecated;
        return this;
    }

    /**
     * @see Parameter#getAllowEmptyValue()
     */
    @Override
    public Boolean getAllowEmptyValue() {
        return this.allowEmptyValue;
    }

    /**
     * @see Parameter#setAllowEmptyValue(Boolean)
     */
    @Override
    public void setAllowEmptyValue(Boolean allowEmptyValue) {
        this.allowEmptyValue = allowEmptyValue;
    }

    /**
     * @see Parameter#allowEmptyValue(Boolean)
     */
    @Override
    public Parameter allowEmptyValue(Boolean allowEmptyValue) {
        this.allowEmptyValue = allowEmptyValue;
        return this;
    }

    /**
     * @see Parameter#getStyle()
     */
    @Override
    public Style getStyle() {
        return this.style;
    }

    /**
     * @see Parameter#setStyle(Style)
     */
    @Override
    public void setStyle(Style style) {
        this.style = style;
    }

    /**
     * @see Parameter#style(Style)
     */
    @Override
    public Parameter style(Style style) {
        this.style = style;
        return this;
    }

    /**
     * @see Parameter#getExplode()
     */
    @Override
    public Boolean getExplode() {
        return this.explode;
    }

    /**
     * @see Parameter#setExplode(Boolean)
     */
    @Override
    public void setExplode(Boolean explode) {
        this.explode = explode;
    }

    /**
     * @see Parameter#explode(Boolean)
     */
    @Override
    public Parameter explode(Boolean explode) {
        this.explode = explode;
        return this;
    }

    /**
     * @see Parameter#getAllowReserved()
     */
    @Override
    public Boolean getAllowReserved() {
        return this.allowReserved;
    }

    /**
     * @see Parameter#setAllowReserved(Boolean)
     */
    @Override
    public void setAllowReserved(Boolean allowReserved) {
        this.allowReserved = allowReserved;
    }

    /**
     * @see Parameter#allowReserved(Boolean)
     */
    @Override
    public Parameter allowReserved(Boolean allowReserved) {
        this.allowReserved = allowReserved;
        return this;
    }

    /**
     * @see Parameter#getSchema()
     */
    @Override
    public Schema getSchema() {
        return this.schema;
    }

    /**
     * @see Parameter#setSchema(Schema)
     */
    @Override
    public void setSchema(Schema schema) {
        this.schema = schema;
    }

    /**
     * @see Parameter#schema(Schema)
     */
    @Override
    public Parameter schema(Schema schema) {
        this.schema = schema;
        return this;
    }

    /**
     * @see Parameter#getExamples()
     */
    @Override
    public Map<String, Example> getExamples() {
        return this.examples;
    }

    /**
     * @see Parameter#setExamples(Map)
     */
    @Override
    public void setExamples(Map<String, Example> examples) {
        this.examples = examples;
    }

    /**
     * @see Parameter#examples(Map)
     */
    @Override
    public Parameter examples(Map<String, Example> examples) {
        this.examples = examples;
        return this;
    }

    /**
     * @see Parameter#addExample(String, Example)
     */
    @Override
    public Parameter addExample(String key, Example example) {
        if (this.examples == null) {
            this.examples = new LinkedHashMap<>();
        }
        this.examples.put(key, example);
        return this;
    }

    /**
     * @see Parameter#getExample()
     */
    @Override
    public Object getExample() {
        return this.example;
    }

    /**
     * @see Parameter#setExample(Object)
     */
    @Override
    public void setExample(Object example) {
        this.example = example;
    }

    /**
     * @see Parameter#example(Object)
     */
    @Override
    public Parameter example(Object example) {
        this.example = example;
        return this;
    }

    /**
     * @see Parameter#getContent()
     */
    @Override
    public Content getContent() {
        return this.content;
    }

    /**
     * @see Parameter#setContent(Content)
     */
    @Override
    public void setContent(Content content) {
        this.content = content;
    }

    /**
     * @see Parameter#content(Content)
     */
    @Override
    public Parameter content(Content content) {
        this.content = content;
        return this;
    }

    /**
     * @see Parameter#getIn()
     */
    @Override
    public In getIn() {
        return in;
    }

    /**
     * @see Parameter#setIn(In)
     */
    @Override
    public void setIn(In in) {
        this.in = in;
    }

    /**
     * @see Parameter#in(In)
     */
    @Override
    public Parameter in(In in) {
        this.in = in;
        return this;
    }

}