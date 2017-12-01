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

package org.wildfly.swarm.microprofile.openapi.models.parameters;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.microprofile.openapi.models.examples.Example;
import org.eclipse.microprofile.openapi.models.media.Content;
import org.eclipse.microprofile.openapi.models.media.Schema;
import org.eclipse.microprofile.openapi.models.parameters.Parameter;
import org.wildfly.swarm.microprofile.openapi.models.ExtensibleImpl;

/**
 * An implementation of the {@link Parameter} OpenAPI model interface.
 */
@SuppressWarnings({"unchecked"})
public abstract class ParameterImpl<T extends Parameter<T>> extends ExtensibleImpl implements Parameter<T> {

    private String $ref;
    private String name;
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
     * @see org.eclipse.microprofile.openapi.models.Reference#setRef(java.lang.String)
     */
    @Override
    public void setRef(String ref) {
        this.$ref = ref;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.Reference#ref(java.lang.String)
     */
    @Override
    public T ref(String ref) {
        this.$ref = ref;
        return (T) this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.parameters.Parameter#getName()
     */
    @Override
    public String getName() {
        return this.name;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.parameters.Parameter#setName(java.lang.String)
     */
    @Override
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.parameters.Parameter#name(java.lang.String)
     */
    @Override
    public T name(String name) {
        this.name = name;
        return (T) this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.parameters.Parameter#getDescription()
     */
    @Override
    public String getDescription() {
        return this.description;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.parameters.Parameter#setDescription(java.lang.String)
     */
    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.parameters.Parameter#description(java.lang.String)
     */
    @Override
    public T description(String description) {
        this.description = description;
        return (T) this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.parameters.Parameter#getRequired()
     */
    @Override
    public Boolean getRequired() {
        return this.required;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.parameters.Parameter#setRequired(java.lang.Boolean)
     */
    @Override
    public void setRequired(Boolean required) {
        this.required = required;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.parameters.Parameter#required(java.lang.Boolean)
     */
    @Override
    public T required(Boolean required) {
        this.required = required;
        return (T) this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.parameters.Parameter#getDeprecated()
     */
    @Override
    public Boolean getDeprecated() {
        return this.deprecated;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.parameters.Parameter#setDeprecated(java.lang.Boolean)
     */
    @Override
    public void setDeprecated(Boolean deprecated) {
        this.deprecated = deprecated;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.parameters.Parameter#deprecated(java.lang.Boolean)
     */
    @Override
    public T deprecated(Boolean deprecated) {
        this.deprecated = deprecated;
        return (T) this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.parameters.Parameter#getAllowEmptyValue()
     */
    @Override
    public Boolean getAllowEmptyValue() {
        return this.allowEmptyValue;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.parameters.Parameter#setAllowEmptyValue(java.lang.Boolean)
     */
    @Override
    public void setAllowEmptyValue(Boolean allowEmptyValue) {
        this.allowEmptyValue = allowEmptyValue;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.parameters.Parameter#allowEmptyValue(java.lang.Boolean)
     */
    @Override
    public T allowEmptyValue(Boolean allowEmptyValue) {
        // TODO Auto-generated method stub
        return (T) this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.parameters.Parameter#getStyle()
     */
    @Override
    public Style getStyle() {
        return this.style;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.parameters.Parameter#setStyle(Style)
     */
    @Override
    public void setStyle(Style style) {
        this.style = style;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.parameters.Parameter#style(Style)
     */
    @Override
    public T style(Style style) {
        this.style = style;
        return (T) this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.parameters.Parameter#getExplode()
     */
    @Override
    public Boolean getExplode() {
        return this.explode;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.parameters.Parameter#setExplode(java.lang.Boolean)
     */
    @Override
    public void setExplode(Boolean explode) {
        this.explode = explode;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.parameters.Parameter#explode(java.lang.Boolean)
     */
    @Override
    public T explode(Boolean explode) {
        this.explode = explode;
        return (T) this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.parameters.Parameter#getAllowReserved()
     */
    @Override
    public Boolean getAllowReserved() {
        return this.allowReserved;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.parameters.Parameter#setAllowReserved(java.lang.Boolean)
     */
    @Override
    public void setAllowReserved(Boolean allowReserved) {
        this.allowReserved = allowReserved;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.parameters.Parameter#allowReserved(java.lang.Boolean)
     */
    @Override
    public T allowReserved(Boolean allowReserved) {
        this.allowReserved = allowReserved;
        return (T) this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.parameters.Parameter#getSchema()
     */
    @Override
    public Schema getSchema() {
        return this.schema;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.parameters.Parameter#setSchema(org.eclipse.microprofile.openapi.models.media.Schema)
     */
    @Override
    public void setSchema(Schema schema) {
        this.schema = schema;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.parameters.Parameter#schema(org.eclipse.microprofile.openapi.models.media.Schema)
     */
    @Override
    public T schema(Schema schema) {
        this.schema = schema;
        return (T) this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.parameters.Parameter#getExamples()
     */
    @Override
    public Map<String, Example> getExamples() {
        return this.examples;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.parameters.Parameter#setExamples(java.util.Map)
     */
    @Override
    public void setExamples(Map<String, Example> examples) {
        this.examples = examples;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.parameters.Parameter#examples(java.util.Map)
     */
    @Override
    public T examples(Map<String, Example> examples) {
        this.examples = examples;
        return (T) this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.parameters.Parameter#addExample(java.lang.String, org.eclipse.microprofile.openapi.models.examples.Example)
     */
    @Override
    public T addExample(String key, Example example) {
        if (this.examples == null) {
            this.examples = new HashMap<>();
        }
        this.examples.put(key, example);
        return (T) this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.parameters.Parameter#getExample()
     */
    @Override
    public Object getExample() {
        return this.example;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.parameters.Parameter#setExample(java.lang.Object)
     */
    @Override
    public void setExample(Object example) {
        this.example = example;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.parameters.Parameter#example(java.lang.Object)
     */
    @Override
    public T example(Object example) {
        this.example = example;
        return (T) this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.parameters.Parameter#getContent()
     */
    @Override
    public Content getContent() {
        return this.content;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.parameters.Parameter#setContent(org.eclipse.microprofile.openapi.models.media.Content)
     */
    @Override
    public void setContent(Content content) {
        this.content = content;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.parameters.Parameter#content(org.eclipse.microprofile.openapi.models.media.Content)
     */
    @Override
    public T content(Content content) {
        this.content = content;
        return (T) this;
    }

}