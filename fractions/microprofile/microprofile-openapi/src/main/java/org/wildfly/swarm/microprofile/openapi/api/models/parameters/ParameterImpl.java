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

package org.wildfly.swarm.microprofile.openapi.api.models.parameters;

import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.microprofile.openapi.models.examples.Example;
import org.eclipse.microprofile.openapi.models.media.Content;
import org.eclipse.microprofile.openapi.models.media.Schema;
import org.eclipse.microprofile.openapi.models.parameters.Parameter;
import org.wildfly.swarm.microprofile.openapi.api.models.ExtensibleImpl;
import org.wildfly.swarm.microprofile.openapi.api.models.ModelImpl;
import org.wildfly.swarm.microprofile.openapi.runtime.OpenApiConstants;

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
     * @see org.eclipse.microprofile.openapi.models.Reference#setRef(java.lang.String)
     */
    @Override
    public void setRef(String ref) {
        if (ref != null && !ref.contains("/")) {
            ref = OpenApiConstants.REF_PREFIX_PARAMETER + ref;
        }
        this.$ref = ref;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.Reference#ref(java.lang.String)
     */
    @Override
    public Parameter ref(String ref) {
        setRef(ref);
        return this;
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
    public Parameter name(String name) {
        this.name = name;
        return this;
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
    public Parameter description(String description) {
        this.description = description;
        return this;
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
    public Parameter required(Boolean required) {
        this.required = required;
        return this;
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
    public Parameter deprecated(Boolean deprecated) {
        this.deprecated = deprecated;
        return this;
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
    public Parameter allowEmptyValue(Boolean allowEmptyValue) {
        this.allowEmptyValue = allowEmptyValue;
        return this;
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
    public Parameter style(Style style) {
        this.style = style;
        return this;
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
    public Parameter explode(Boolean explode) {
        this.explode = explode;
        return this;
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
    public Parameter allowReserved(Boolean allowReserved) {
        this.allowReserved = allowReserved;
        return this;
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
    public Parameter schema(Schema schema) {
        this.schema = schema;
        return this;
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
    public Parameter examples(Map<String, Example> examples) {
        this.examples = examples;
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.parameters.Parameter#addExample(java.lang.String, org.eclipse.microprofile.openapi.models.examples.Example)
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
    public Parameter example(Object example) {
        this.example = example;
        return this;
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
    public Parameter content(Content content) {
        this.content = content;
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.parameters.Parameter#getIn()
     */
    @Override
    public In getIn() {
        return in;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.parameters.Parameter#setIn(org.eclipse.microprofile.openapi.models.parameters.Parameter.In)
     */
    @Override
    public void setIn(In in) {
        this.in = in;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.parameters.Parameter#in(org.eclipse.microprofile.openapi.models.parameters.Parameter.In)
     */
    @Override
    public Parameter in(In in) {
        this.in = in;
        return this;
    }

}