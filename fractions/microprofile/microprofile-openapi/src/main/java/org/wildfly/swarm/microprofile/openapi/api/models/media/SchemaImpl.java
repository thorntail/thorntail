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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.microprofile.openapi.models.ExternalDocumentation;
import org.eclipse.microprofile.openapi.models.media.Content;
import org.eclipse.microprofile.openapi.models.media.Discriminator;
import org.eclipse.microprofile.openapi.models.media.Schema;
import org.eclipse.microprofile.openapi.models.media.XML;
import org.wildfly.swarm.microprofile.openapi.api.models.ExtensibleImpl;
import org.wildfly.swarm.microprofile.openapi.api.models.ModelImpl;
import org.wildfly.swarm.microprofile.openapi.runtime.OpenApiConstants;

/**
 * An implementation of the {@link Content} OpenAPI model interface.
 */
public class SchemaImpl extends ExtensibleImpl implements Schema, ModelImpl {

    private String $ref;
    private String format;
    private String title;
    private String description;
    private Object defaultValue;
    private BigDecimal multipleOf;
    private BigDecimal maximum;
    private Boolean exclusiveMaximum;
    private BigDecimal minimum;
    private Boolean exclusiveMinimum;
    private Integer maxLength;
    private Integer minLength;
    private String pattern;
    private Integer maxItems;
    private Integer minItems;
    private Boolean uniqueItems;
    private Integer maxProperties;
    private Integer minProperties;
    private List<String> required;
    private List<Object> enumeration;
    private SchemaType type;
    private Schema items;
    private List<Schema> allOf;
    private Map<String, Schema> properties;
    private Schema additionalPropertiesSchema;
    private Boolean additionalPropertiesBoolean;
    private Boolean readOnly;
    private XML xml;
    private ExternalDocumentation externalDocs;
    private Object example;
    private List<Schema> oneOf;
    private List<Schema> anyOf;
    private Schema not;
    private Discriminator discriminator;
    private Boolean nullable;
    private Boolean writeOnly;
    private Boolean deprecated;

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
            ref = OpenApiConstants.REF_PREFIX_SCHEMA + ref;
        }
        this.$ref = ref;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.Reference#ref(java.lang.String)
     */
    @Override
    public Schema ref(String ref) {
        setRef(ref);
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#getDiscriminator()
     */
    @Override
    public Discriminator getDiscriminator() {
        return this.discriminator;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#setDiscriminator(org.eclipse.microprofile.openapi.models.media.Discriminator)
     */
    @Override
    public void setDiscriminator(Discriminator discriminator) {
        this.discriminator = discriminator;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#discriminator(org.eclipse.microprofile.openapi.models.media.Discriminator)
     */
    @Override
    public Schema discriminator(Discriminator discriminator) {
        this.discriminator = discriminator;
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#getTitle()
     */
    @Override
    public String getTitle() {
        return this.title;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#setTitle(java.lang.String)
     */
    @Override
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#title(java.lang.String)
     */
    @Override
    public Schema title(String title) {
        this.title = title;
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#getDefaultValue()
     */
    @Override
    public Object getDefaultValue() {
        return this.defaultValue;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#setDefaultValue(java.lang.Object)
     */
    @Override
    public void setDefaultValue(Object defaultValue) {
        this.defaultValue = defaultValue;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#defaultValue(java.lang.Object)
     */
    @Override
    public Schema defaultValue(Object defaultValue) {
        this.defaultValue = defaultValue;
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#getEnumeration()
     */
    @Override
    public List<Object> getEnumeration() {
        return this.enumeration;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#setEnumeration(java.util.List)
     */
    @Override
    public void setEnumeration(List<Object> enumeration) {
        this.enumeration = enumeration;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#enumeration(java.util.List)
     */
    @Override
    public Schema enumeration(List<Object> enumeration) {
        this.enumeration = enumeration;
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#addEnumeration(java.lang.Object)
     */
    @Override
    public Schema addEnumeration(Object enumeration) {
        if (this.enumeration == null) {
            this.enumeration = new ArrayList<>();
        }
        this.enumeration.add(enumeration);
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#getMultipleOf()
     */
    @Override
    public BigDecimal getMultipleOf() {
        return this.multipleOf;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#setMultipleOf(java.math.BigDecimal)
     */
    @Override
    public void setMultipleOf(BigDecimal multipleOf) {
        this.multipleOf = multipleOf;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#multipleOf(java.math.BigDecimal)
     */
    @Override
    public Schema multipleOf(BigDecimal multipleOf) {
        this.multipleOf = multipleOf;
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#getMaximum()
     */
    @Override
    public BigDecimal getMaximum() {
        return this.maximum;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#setMaximum(java.math.BigDecimal)
     */
    @Override
    public void setMaximum(BigDecimal maximum) {
        this.maximum = maximum;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#maximum(java.math.BigDecimal)
     */
    @Override
    public Schema maximum(BigDecimal maximum) {
        this.maximum = maximum;
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#getExclusiveMaximum()
     */
    @Override
    public Boolean getExclusiveMaximum() {
        return this.exclusiveMaximum;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#setExclusiveMaximum(java.lang.Boolean)
     */
    @Override
    public void setExclusiveMaximum(Boolean exclusiveMaximum) {
        this.exclusiveMaximum = exclusiveMaximum;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#exclusiveMaximum(java.lang.Boolean)
     */
    @Override
    public Schema exclusiveMaximum(Boolean exclusiveMaximum) {
        this.exclusiveMaximum = exclusiveMaximum;
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#getMinimum()
     */
    @Override
    public BigDecimal getMinimum() {
        return this.minimum;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#setMinimum(java.math.BigDecimal)
     */
    @Override
    public void setMinimum(BigDecimal minimum) {
        this.minimum = minimum;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#minimum(java.math.BigDecimal)
     */
    @Override
    public Schema minimum(BigDecimal minimum) {
        this.minimum = minimum;
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#getExclusiveMinimum()
     */
    @Override
    public Boolean getExclusiveMinimum() {
        return this.exclusiveMinimum;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#setExclusiveMinimum(java.lang.Boolean)
     */
    @Override
    public void setExclusiveMinimum(Boolean exclusiveMinimum) {
        this.exclusiveMinimum = exclusiveMinimum;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#exclusiveMinimum(java.lang.Boolean)
     */
    @Override
    public Schema exclusiveMinimum(Boolean exclusiveMinimum) {
        this.exclusiveMinimum = exclusiveMinimum;
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#getMaxLength()
     */
    @Override
    public Integer getMaxLength() {
        return this.maxLength;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#setMaxLength(java.lang.Integer)
     */
    @Override
    public void setMaxLength(Integer maxLength) {
        this.maxLength = maxLength;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#maxLength(java.lang.Integer)
     */
    @Override
    public Schema maxLength(Integer maxLength) {
        this.maxLength = maxLength;
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#getMinLength()
     */
    @Override
    public Integer getMinLength() {
        return this.minLength;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#setMinLength(java.lang.Integer)
     */
    @Override
    public void setMinLength(Integer minLength) {
        this.minLength = minLength;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#minLength(java.lang.Integer)
     */
    @Override
    public Schema minLength(Integer minLength) {
        this.minLength = minLength;
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#getPattern()
     */
    @Override
    public String getPattern() {
        return this.pattern;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#setPattern(java.lang.String)
     */
    @Override
    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#pattern(java.lang.String)
     */
    @Override
    public Schema pattern(String pattern) {
        this.pattern = pattern;
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#getMaxItems()
     */
    @Override
    public Integer getMaxItems() {
        return this.maxItems;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#setMaxItems(java.lang.Integer)
     */
    @Override
    public void setMaxItems(Integer maxItems) {
        this.maxItems = maxItems;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#maxItems(java.lang.Integer)
     */
    @Override
    public Schema maxItems(Integer maxItems) {
        this.maxItems = maxItems;
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#getMinItems()
     */
    @Override
    public Integer getMinItems() {
        return this.minItems;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#setMinItems(java.lang.Integer)
     */
    @Override
    public void setMinItems(Integer minItems) {
        this.minItems = minItems;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#minItems(java.lang.Integer)
     */
    @Override
    public Schema minItems(Integer minItems) {
        this.minItems = minItems;
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#getUniqueItems()
     */
    @Override
    public Boolean getUniqueItems() {
        return this.uniqueItems;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#setUniqueItems(java.lang.Boolean)
     */
    @Override
    public void setUniqueItems(Boolean uniqueItems) {
        this.uniqueItems = uniqueItems;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#uniqueItems(java.lang.Boolean)
     */
    @Override
    public Schema uniqueItems(Boolean uniqueItems) {
        this.uniqueItems = uniqueItems;
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#getMaxProperties()
     */
    @Override
    public Integer getMaxProperties() {
        return this.maxProperties;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#setMaxProperties(java.lang.Integer)
     */
    @Override
    public void setMaxProperties(Integer maxProperties) {
        this.maxProperties = maxProperties;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#maxProperties(java.lang.Integer)
     */
    @Override
    public Schema maxProperties(Integer maxProperties) {
        this.maxProperties = maxProperties;
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#getMinProperties()
     */
    @Override
    public Integer getMinProperties() {
        return this.minProperties;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#setMinProperties(java.lang.Integer)
     */
    @Override
    public void setMinProperties(Integer minProperties) {
        this.minProperties = minProperties;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#minProperties(java.lang.Integer)
     */
    @Override
    public Schema minProperties(Integer minProperties) {
        this.minProperties = minProperties;
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#getRequired()
     */
    @Override
    public List<String> getRequired() {
        return this.required;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#setRequired(java.util.List)
     */
    @Override
    public void setRequired(List<String> required) {
        this.required = required;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#required(java.util.List)
     */
    @Override
    public Schema required(List<String> required) {
        this.required = required;
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#addRequired(java.lang.String)
     */
    @Override
    public Schema addRequired(String required) {
        if (this.required == null) {
            this.required = new ArrayList<>();
        }
        this.required.add(required);
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#getType()
     */
    @Override
    public SchemaType getType() {
        return this.type;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#setType(org.eclipse.microprofile.openapi.models.media.Schema.SchemaType)
     */
    @Override
    public void setType(SchemaType type) {
        this.type = type;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#type(org.eclipse.microprofile.openapi.models.media.Schema.SchemaType)
     */
    @Override
    public Schema type(SchemaType type) {
        this.type = type;
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#getNot()
     */
    @Override
    public Schema getNot() {
        return this.not;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#setNot(org.eclipse.microprofile.openapi.models.media.Schema)
     */
    @Override
    public void setNot(Schema not) {
        this.not = not;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#not(org.eclipse.microprofile.openapi.models.media.Schema)
     */
    @Override
    public Schema not(Schema not) {
        this.not = not;
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#getProperties()
     */
    @Override
    public Map<String, Schema> getProperties() {
        return this.properties;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#setProperties(java.util.Map)
     */
    @Override
    public void setProperties(Map<String, Schema> properties) {
        this.properties = properties;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#properties(java.util.Map)
     */
    @Override
    public Schema properties(Map<String, Schema> properties) {
        this.properties = properties;
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#addProperty(java.lang.String, org.eclipse.microprofile.openapi.models.media.Schema)
     */
    @Override
    public Schema addProperty(String key, Schema propertySchema) {
        if (this.properties == null) {
            this.properties = new LinkedHashMap<>();
        }
        this.properties.put(key, propertySchema);
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#getAdditionalProperties()
     */
    @Override
    public Object getAdditionalProperties() {
        if (this.additionalPropertiesSchema != null) {
            return this.additionalPropertiesSchema;
        } else {
            return this.additionalPropertiesBoolean;
        }
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#setAdditionalProperties(org.eclipse.microprofile.openapi.models.media.Schema)
     */
    @Override
    public void setAdditionalProperties(Schema additionalProperties) {
        this.additionalPropertiesBoolean = null;
        this.additionalPropertiesSchema = additionalProperties;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#setAdditionalProperties(java.lang.Boolean)
     */
    @Override
    public void setAdditionalProperties(Boolean additionalProperties) {
        this.additionalPropertiesSchema = null;
        this.additionalPropertiesBoolean = additionalProperties;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#additionalProperties(org.eclipse.microprofile.openapi.models.media.Schema)
     */
    @Override
    public Schema additionalProperties(Schema additionalProperties) {
        this.additionalPropertiesBoolean = null;
        this.additionalPropertiesSchema = additionalProperties;
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#additionalProperties(java.lang.Boolean)
     */
    @Override
    public Schema additionalProperties(Boolean additionalProperties) {
        this.additionalPropertiesSchema = null;
        this.additionalPropertiesBoolean = additionalProperties;
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#getDescription()
     */
    @Override
    public String getDescription() {
        return this.description;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#setDescription(java.lang.String)
     */
    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#description(java.lang.String)
     */
    @Override
    public Schema description(String description) {
        this.description = description;
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#getFormat()
     */
    @Override
    public String getFormat() {
        return this.format;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#setFormat(java.lang.String)
     */
    @Override
    public void setFormat(String format) {
        this.format = format;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#format(java.lang.String)
     */
    @Override
    public Schema format(String format) {
        this.format = format;
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#getNullable()
     */
    @Override
    public Boolean getNullable() {
        return this.nullable;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#setNullable(java.lang.Boolean)
     */
    @Override
    public void setNullable(Boolean nullable) {
        this.nullable = nullable;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#nullable(java.lang.Boolean)
     */
    @Override
    public Schema nullable(Boolean nullable) {
        this.nullable = nullable;
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#getReadOnly()
     */
    @Override
    public Boolean getReadOnly() {
        return this.readOnly;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#setReadOnly(java.lang.Boolean)
     */
    @Override
    public void setReadOnly(Boolean readOnly) {
        this.readOnly = readOnly;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#readOnly(java.lang.Boolean)
     */
    @Override
    public Schema readOnly(Boolean readOnly) {
        this.readOnly = readOnly;
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#getWriteOnly()
     */
    @Override
    public Boolean getWriteOnly() {
        return this.writeOnly;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#setWriteOnly(java.lang.Boolean)
     */
    @Override
    public void setWriteOnly(Boolean writeOnly) {
        this.writeOnly = writeOnly;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#writeOnly(java.lang.Boolean)
     */
    @Override
    public Schema writeOnly(Boolean writeOnly) {
        this.writeOnly = writeOnly;
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#getExample()
     */
    @Override
    public Object getExample() {
        return this.example;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#setExample(java.lang.Object)
     */
    @Override
    public void setExample(Object example) {
        this.example = example;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#example(java.lang.Object)
     */
    @Override
    public Schema example(Object example) {
        this.example = example;
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#getExternalDocs()
     */
    @Override
    public ExternalDocumentation getExternalDocs() {
        return this.externalDocs;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#setExternalDocs(org.eclipse.microprofile.openapi.models.ExternalDocumentation)
     */
    @Override
    public void setExternalDocs(ExternalDocumentation externalDocs) {
        this.externalDocs = externalDocs;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#externalDocs(org.eclipse.microprofile.openapi.models.ExternalDocumentation)
     */
    @Override
    public Schema externalDocs(ExternalDocumentation externalDocs) {
        this.externalDocs = externalDocs;
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#getDeprecated()
     */
    @Override
    public Boolean getDeprecated() {
        return this.deprecated;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#setDeprecated(java.lang.Boolean)
     */
    @Override
    public void setDeprecated(Boolean deprecated) {
        this.deprecated = deprecated;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#deprecated(java.lang.Boolean)
     */
    @Override
    public Schema deprecated(Boolean deprecated) {
        this.deprecated = deprecated;
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#getXml()
     */
    @Override
    public XML getXml() {
        return this.xml;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#setXml(org.eclipse.microprofile.openapi.models.media.XML)
     */
    @Override
    public void setXml(XML xml) {
        this.xml = xml;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#xml(org.eclipse.microprofile.openapi.models.media.XML)
     */
    @Override
    public Schema xml(XML xml) {
        this.xml = xml;
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#getItems()
     */
    @Override
    public Schema getItems() {
        return this.items;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#setItems(org.eclipse.microprofile.openapi.models.media.Schema)
     */
    @Override
    public void setItems(Schema items) {
        this.items = items;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#items(org.eclipse.microprofile.openapi.models.media.Schema)
     */
    @Override
    public Schema items(Schema items) {
        this.items = items;
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#getAllOf()
     */
    @Override
    public List<Schema> getAllOf() {
        return this.allOf;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#setAllOf(java.util.List)
     */
    @Override
    public void setAllOf(List<Schema> allOf) {
        this.allOf = allOf;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#allOf(java.util.List)
     */
    @Override
    public Schema allOf(List<Schema> allOf) {
        this.allOf = allOf;
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#addAllOf(org.eclipse.microprofile.openapi.models.media.Schema)
     */
    @Override
    public Schema addAllOf(Schema allOf) {
        if (this.allOf == null) {
            this.allOf = new ArrayList<>();
        }
        this.allOf.add(allOf);
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#getAnyOf()
     */
    @Override
    public List<Schema> getAnyOf() {
        return this.anyOf;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#setAnyOf(java.util.List)
     */
    @Override
    public void setAnyOf(List<Schema> anyOf) {
        this.anyOf = anyOf;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#anyOf(java.util.List)
     */
    @Override
    public Schema anyOf(List<Schema> anyOf) {
        this.anyOf = anyOf;
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#addAnyOf(org.eclipse.microprofile.openapi.models.media.Schema)
     */
    @Override
    public Schema addAnyOf(Schema anyOf) {
        if (this.anyOf == null) {
            this.anyOf = new ArrayList<>();
        }
        this.anyOf.add(anyOf);
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#getOneOf()
     */
    @Override
    public List<Schema> getOneOf() {
        return this.oneOf;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#setOneOf(java.util.List)
     */
    @Override
    public void setOneOf(List<Schema> oneOf) {
        this.oneOf = oneOf;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#oneOf(java.util.List)
     */
    @Override
    public Schema oneOf(List<Schema> oneOf) {
        this.oneOf = oneOf;
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#addOneOf(org.eclipse.microprofile.openapi.models.media.Schema)
     */
    @Override
    public Schema addOneOf(Schema oneOf) {
        if (this.oneOf == null) {
            this.oneOf = new ArrayList<>();
        }
        this.oneOf.add(oneOf);
        return this;
    }

}
