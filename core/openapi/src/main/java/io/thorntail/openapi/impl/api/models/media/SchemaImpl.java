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

package io.thorntail.openapi.impl.api.models.media;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.thorntail.openapi.impl.api.models.ExtensibleImpl;
import org.eclipse.microprofile.openapi.models.ExternalDocumentation;
import org.eclipse.microprofile.openapi.models.media.Content;
import org.eclipse.microprofile.openapi.models.media.Discriminator;
import org.eclipse.microprofile.openapi.models.media.Schema;
import org.eclipse.microprofile.openapi.models.media.XML;
import io.thorntail.openapi.impl.api.models.ModelImpl;
import io.thorntail.openapi.impl.OpenApiConstants;

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
     * @see org.eclipse.microprofile.openapi.models.Reference#setRef(String)
     */
    @Override
    public void setRef(String ref) {
        if (ref != null && !ref.contains("/")) {
            ref = OpenApiConstants.REF_PREFIX_SCHEMA + ref;
        }
        this.$ref = ref;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.Reference#ref(String)
     */
    @Override
    public Schema ref(String ref) {
        setRef(ref);
        return this;
    }

    /**
     * @see Schema#getDiscriminator()
     */
    @Override
    public Discriminator getDiscriminator() {
        return this.discriminator;
    }

    /**
     * @see Schema#setDiscriminator(Discriminator)
     */
    @Override
    public void setDiscriminator(Discriminator discriminator) {
        this.discriminator = discriminator;
    }

    /**
     * @see Schema#discriminator(Discriminator)
     */
    @Override
    public Schema discriminator(Discriminator discriminator) {
        this.discriminator = discriminator;
        return this;
    }

    /**
     * @see Schema#getTitle()
     */
    @Override
    public String getTitle() {
        return this.title;
    }

    /**
     * @see Schema#setTitle(String)
     */
    @Override
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * @see Schema#title(String)
     */
    @Override
    public Schema title(String title) {
        this.title = title;
        return this;
    }

    /**
     * @see Schema#getDefaultValue()
     */
    @Override
    public Object getDefaultValue() {
        return this.defaultValue;
    }

    /**
     * @see Schema#setDefaultValue(Object)
     */
    @Override
    public void setDefaultValue(Object defaultValue) {
        this.defaultValue = defaultValue;
    }

    /**
     * @see Schema#defaultValue(Object)
     */
    @Override
    public Schema defaultValue(Object defaultValue) {
        this.defaultValue = defaultValue;
        return this;
    }

    /**
     * @see Schema#getEnumeration()
     */
    @Override
    public List<Object> getEnumeration() {
        return this.enumeration;
    }

    /**
     * @see Schema#setEnumeration(List)
     */
    @Override
    public void setEnumeration(List<Object> enumeration) {
        this.enumeration = enumeration;
    }

    /**
     * @see Schema#enumeration(List)
     */
    @Override
    public Schema enumeration(List<Object> enumeration) {
        this.enumeration = enumeration;
        return this;
    }

    /**
     * @see Schema#addEnumeration(Object)
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
     * @see Schema#getMultipleOf()
     */
    @Override
    public BigDecimal getMultipleOf() {
        return this.multipleOf;
    }

    /**
     * @see Schema#setMultipleOf(BigDecimal)
     */
    @Override
    public void setMultipleOf(BigDecimal multipleOf) {
        this.multipleOf = multipleOf;
    }

    /**
     * @see Schema#multipleOf(BigDecimal)
     */
    @Override
    public Schema multipleOf(BigDecimal multipleOf) {
        this.multipleOf = multipleOf;
        return this;
    }

    /**
     * @see Schema#getMaximum()
     */
    @Override
    public BigDecimal getMaximum() {
        return this.maximum;
    }

    /**
     * @see Schema#setMaximum(BigDecimal)
     */
    @Override
    public void setMaximum(BigDecimal maximum) {
        this.maximum = maximum;
    }

    /**
     * @see Schema#maximum(BigDecimal)
     */
    @Override
    public Schema maximum(BigDecimal maximum) {
        this.maximum = maximum;
        return this;
    }

    /**
     * @see Schema#getExclusiveMaximum()
     */
    @Override
    public Boolean getExclusiveMaximum() {
        return this.exclusiveMaximum;
    }

    /**
     * @see Schema#setExclusiveMaximum(Boolean)
     */
    @Override
    public void setExclusiveMaximum(Boolean exclusiveMaximum) {
        this.exclusiveMaximum = exclusiveMaximum;
    }

    /**
     * @see Schema#exclusiveMaximum(Boolean)
     */
    @Override
    public Schema exclusiveMaximum(Boolean exclusiveMaximum) {
        this.exclusiveMaximum = exclusiveMaximum;
        return this;
    }

    /**
     * @see Schema#getMinimum()
     */
    @Override
    public BigDecimal getMinimum() {
        return this.minimum;
    }

    /**
     * @see Schema#setMinimum(BigDecimal)
     */
    @Override
    public void setMinimum(BigDecimal minimum) {
        this.minimum = minimum;
    }

    /**
     * @see Schema#minimum(BigDecimal)
     */
    @Override
    public Schema minimum(BigDecimal minimum) {
        this.minimum = minimum;
        return this;
    }

    /**
     * @see Schema#getExclusiveMinimum()
     */
    @Override
    public Boolean getExclusiveMinimum() {
        return this.exclusiveMinimum;
    }

    /**
     * @see Schema#setExclusiveMinimum(Boolean)
     */
    @Override
    public void setExclusiveMinimum(Boolean exclusiveMinimum) {
        this.exclusiveMinimum = exclusiveMinimum;
    }

    /**
     * @see Schema#exclusiveMinimum(Boolean)
     */
    @Override
    public Schema exclusiveMinimum(Boolean exclusiveMinimum) {
        this.exclusiveMinimum = exclusiveMinimum;
        return this;
    }

    /**
     * @see Schema#getMaxLength()
     */
    @Override
    public Integer getMaxLength() {
        return this.maxLength;
    }

    /**
     * @see Schema#setMaxLength(Integer)
     */
    @Override
    public void setMaxLength(Integer maxLength) {
        this.maxLength = maxLength;
    }

    /**
     * @see Schema#maxLength(Integer)
     */
    @Override
    public Schema maxLength(Integer maxLength) {
        this.maxLength = maxLength;
        return this;
    }

    /**
     * @see Schema#getMinLength()
     */
    @Override
    public Integer getMinLength() {
        return this.minLength;
    }

    /**
     * @see Schema#setMinLength(Integer)
     */
    @Override
    public void setMinLength(Integer minLength) {
        this.minLength = minLength;
    }

    /**
     * @see Schema#minLength(Integer)
     */
    @Override
    public Schema minLength(Integer minLength) {
        this.minLength = minLength;
        return this;
    }

    /**
     * @see Schema#getPattern()
     */
    @Override
    public String getPattern() {
        return this.pattern;
    }

    /**
     * @see Schema#setPattern(String)
     */
    @Override
    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    /**
     * @see Schema#pattern(String)
     */
    @Override
    public Schema pattern(String pattern) {
        this.pattern = pattern;
        return this;
    }

    /**
     * @see Schema#getMaxItems()
     */
    @Override
    public Integer getMaxItems() {
        return this.maxItems;
    }

    /**
     * @see Schema#setMaxItems(Integer)
     */
    @Override
    public void setMaxItems(Integer maxItems) {
        this.maxItems = maxItems;
    }

    /**
     * @see Schema#maxItems(Integer)
     */
    @Override
    public Schema maxItems(Integer maxItems) {
        this.maxItems = maxItems;
        return this;
    }

    /**
     * @see Schema#getMinItems()
     */
    @Override
    public Integer getMinItems() {
        return this.minItems;
    }

    /**
     * @see Schema#setMinItems(Integer)
     */
    @Override
    public void setMinItems(Integer minItems) {
        this.minItems = minItems;
    }

    /**
     * @see Schema#minItems(Integer)
     */
    @Override
    public Schema minItems(Integer minItems) {
        this.minItems = minItems;
        return this;
    }

    /**
     * @see Schema#getUniqueItems()
     */
    @Override
    public Boolean getUniqueItems() {
        return this.uniqueItems;
    }

    /**
     * @see Schema#setUniqueItems(Boolean)
     */
    @Override
    public void setUniqueItems(Boolean uniqueItems) {
        this.uniqueItems = uniqueItems;
    }

    /**
     * @see Schema#uniqueItems(Boolean)
     */
    @Override
    public Schema uniqueItems(Boolean uniqueItems) {
        this.uniqueItems = uniqueItems;
        return this;
    }

    /**
     * @see Schema#getMaxProperties()
     */
    @Override
    public Integer getMaxProperties() {
        return this.maxProperties;
    }

    /**
     * @see Schema#setMaxProperties(Integer)
     */
    @Override
    public void setMaxProperties(Integer maxProperties) {
        this.maxProperties = maxProperties;
    }

    /**
     * @see Schema#maxProperties(Integer)
     */
    @Override
    public Schema maxProperties(Integer maxProperties) {
        this.maxProperties = maxProperties;
        return this;
    }

    /**
     * @see Schema#getMinProperties()
     */
    @Override
    public Integer getMinProperties() {
        return this.minProperties;
    }

    /**
     * @see Schema#setMinProperties(Integer)
     */
    @Override
    public void setMinProperties(Integer minProperties) {
        this.minProperties = minProperties;
    }

    /**
     * @see Schema#minProperties(Integer)
     */
    @Override
    public Schema minProperties(Integer minProperties) {
        this.minProperties = minProperties;
        return this;
    }

    /**
     * @see Schema#getRequired()
     */
    @Override
    public List<String> getRequired() {
        return this.required;
    }

    /**
     * @see Schema#setRequired(List)
     */
    @Override
    public void setRequired(List<String> required) {
        this.required = required;
    }

    /**
     * @see Schema#required(List)
     */
    @Override
    public Schema required(List<String> required) {
        this.required = required;
        return this;
    }

    /**
     * @see Schema#addRequired(String)
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
     * @see Schema#getType()
     */
    @Override
    public SchemaType getType() {
        return this.type;
    }

    /**
     * @see Schema#setType(SchemaType)
     */
    @Override
    public void setType(SchemaType type) {
        this.type = type;
    }

    /**
     * @see Schema#type(SchemaType)
     */
    @Override
    public Schema type(SchemaType type) {
        this.type = type;
        return this;
    }

    /**
     * @see Schema#getNot()
     */
    @Override
    public Schema getNot() {
        return this.not;
    }

    /**
     * @see Schema#setNot(Schema)
     */
    @Override
    public void setNot(Schema not) {
        this.not = not;
    }

    /**
     * @see Schema#not(Schema)
     */
    @Override
    public Schema not(Schema not) {
        this.not = not;
        return this;
    }

    /**
     * @see Schema#getProperties()
     */
    @Override
    public Map<String, Schema> getProperties() {
        return this.properties;
    }

    /**
     * @see Schema#setProperties(Map)
     */
    @Override
    public void setProperties(Map<String, Schema> properties) {
        this.properties = properties;
    }

    /**
     * @see Schema#properties(Map)
     */
    @Override
    public Schema properties(Map<String, Schema> properties) {
        this.properties = properties;
        return this;
    }

    /**
     * @see Schema#addProperty(String, Schema)
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
     * @see Schema#getAdditionalProperties()
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
     * @see Schema#setAdditionalProperties(Schema)
     */
    @Override
    public void setAdditionalProperties(Schema additionalProperties) {
        this.additionalPropertiesBoolean = null;
        this.additionalPropertiesSchema = additionalProperties;
    }

    /**
     * @see Schema#setAdditionalProperties(Boolean)
     */
    @Override
    public void setAdditionalProperties(Boolean additionalProperties) {
        this.additionalPropertiesSchema = null;
        this.additionalPropertiesBoolean = additionalProperties;
    }

    /**
     * @see Schema#additionalProperties(Schema)
     */
    @Override
    public Schema additionalProperties(Schema additionalProperties) {
        this.additionalPropertiesBoolean = null;
        this.additionalPropertiesSchema = additionalProperties;
        return this;
    }

    /**
     * @see Schema#additionalProperties(Boolean)
     */
    @Override
    public Schema additionalProperties(Boolean additionalProperties) {
        this.additionalPropertiesSchema = null;
        this.additionalPropertiesBoolean = additionalProperties;
        return this;
    }

    /**
     * @see Schema#getDescription()
     */
    @Override
    public String getDescription() {
        return this.description;
    }

    /**
     * @see Schema#setDescription(String)
     */
    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @see Schema#description(String)
     */
    @Override
    public Schema description(String description) {
        this.description = description;
        return this;
    }

    /**
     * @see Schema#getFormat()
     */
    @Override
    public String getFormat() {
        return this.format;
    }

    /**
     * @see Schema#setFormat(String)
     */
    @Override
    public void setFormat(String format) {
        this.format = format;
    }

    /**
     * @see Schema#format(String)
     */
    @Override
    public Schema format(String format) {
        this.format = format;
        return this;
    }

    /**
     * @see Schema#getNullable()
     */
    @Override
    public Boolean getNullable() {
        return this.nullable;
    }

    /**
     * @see Schema#setNullable(Boolean)
     */
    @Override
    public void setNullable(Boolean nullable) {
        this.nullable = nullable;
    }

    /**
     * @see Schema#nullable(Boolean)
     */
    @Override
    public Schema nullable(Boolean nullable) {
        this.nullable = nullable;
        return this;
    }

    /**
     * @see Schema#getReadOnly()
     */
    @Override
    public Boolean getReadOnly() {
        return this.readOnly;
    }

    /**
     * @see Schema#setReadOnly(Boolean)
     */
    @Override
    public void setReadOnly(Boolean readOnly) {
        this.readOnly = readOnly;
    }

    /**
     * @see Schema#readOnly(Boolean)
     */
    @Override
    public Schema readOnly(Boolean readOnly) {
        this.readOnly = readOnly;
        return this;
    }

    /**
     * @see Schema#getWriteOnly()
     */
    @Override
    public Boolean getWriteOnly() {
        return this.writeOnly;
    }

    /**
     * @see Schema#setWriteOnly(Boolean)
     */
    @Override
    public void setWriteOnly(Boolean writeOnly) {
        this.writeOnly = writeOnly;
    }

    /**
     * @see Schema#writeOnly(Boolean)
     */
    @Override
    public Schema writeOnly(Boolean writeOnly) {
        this.writeOnly = writeOnly;
        return this;
    }

    /**
     * @see Schema#getExample()
     */
    @Override
    public Object getExample() {
        return this.example;
    }

    /**
     * @see Schema#setExample(Object)
     */
    @Override
    public void setExample(Object example) {
        this.example = example;
    }

    /**
     * @see Schema#example(Object)
     */
    @Override
    public Schema example(Object example) {
        this.example = example;
        return this;
    }

    /**
     * @see Schema#getExternalDocs()
     */
    @Override
    public ExternalDocumentation getExternalDocs() {
        return this.externalDocs;
    }

    /**
     * @see Schema#setExternalDocs(ExternalDocumentation)
     */
    @Override
    public void setExternalDocs(ExternalDocumentation externalDocs) {
        this.externalDocs = externalDocs;
    }

    /**
     * @see Schema#externalDocs(ExternalDocumentation)
     */
    @Override
    public Schema externalDocs(ExternalDocumentation externalDocs) {
        this.externalDocs = externalDocs;
        return this;
    }

    /**
     * @see Schema#getDeprecated()
     */
    @Override
    public Boolean getDeprecated() {
        return this.deprecated;
    }

    /**
     * @see Schema#setDeprecated(Boolean)
     */
    @Override
    public void setDeprecated(Boolean deprecated) {
        this.deprecated = deprecated;
    }

    /**
     * @see Schema#deprecated(Boolean)
     */
    @Override
    public Schema deprecated(Boolean deprecated) {
        this.deprecated = deprecated;
        return this;
    }

    /**
     * @see Schema#getXml()
     */
    @Override
    public XML getXml() {
        return this.xml;
    }

    /**
     * @see Schema#setXml(XML)
     */
    @Override
    public void setXml(XML xml) {
        this.xml = xml;
    }

    /**
     * @see Schema#xml(XML)
     */
    @Override
    public Schema xml(XML xml) {
        this.xml = xml;
        return this;
    }

    /**
     * @see Schema#getItems()
     */
    @Override
    public Schema getItems() {
        return this.items;
    }

    /**
     * @see Schema#setItems(Schema)
     */
    @Override
    public void setItems(Schema items) {
        this.items = items;
    }

    /**
     * @see Schema#items(Schema)
     */
    @Override
    public Schema items(Schema items) {
        this.items = items;
        return this;
    }

    /**
     * @see Schema#getAllOf()
     */
    @Override
    public List<Schema> getAllOf() {
        return this.allOf;
    }

    /**
     * @see Schema#setAllOf(List)
     */
    @Override
    public void setAllOf(List<Schema> allOf) {
        this.allOf = allOf;
    }

    /**
     * @see Schema#allOf(List)
     */
    @Override
    public Schema allOf(List<Schema> allOf) {
        this.allOf = allOf;
        return this;
    }

    /**
     * @see Schema#addAllOf(Schema)
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
     * @see Schema#getAnyOf()
     */
    @Override
    public List<Schema> getAnyOf() {
        return this.anyOf;
    }

    /**
     * @see Schema#setAnyOf(List)
     */
    @Override
    public void setAnyOf(List<Schema> anyOf) {
        this.anyOf = anyOf;
    }

    /**
     * @see Schema#anyOf(List)
     */
    @Override
    public Schema anyOf(List<Schema> anyOf) {
        this.anyOf = anyOf;
        return this;
    }

    /**
     * @see Schema#addAnyOf(Schema)
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
     * @see Schema#getOneOf()
     */
    @Override
    public List<Schema> getOneOf() {
        return this.oneOf;
    }

    /**
     * @see Schema#setOneOf(List)
     */
    @Override
    public void setOneOf(List<Schema> oneOf) {
        this.oneOf = oneOf;
    }

    /**
     * @see Schema#oneOf(List)
     */
    @Override
    public Schema oneOf(List<Schema> oneOf) {
        this.oneOf = oneOf;
        return this;
    }

    /**
     * @see Schema#addOneOf(Schema)
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
