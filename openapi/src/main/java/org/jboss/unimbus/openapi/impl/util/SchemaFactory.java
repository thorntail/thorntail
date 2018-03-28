package org.jboss.unimbus.openapi.impl.util;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.microprofile.openapi.models.ExternalDocumentation;
import org.eclipse.microprofile.openapi.models.media.Schema;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationValue;
import org.jboss.jandex.ClassType;
import org.jboss.jandex.IndexView;
import org.jboss.jandex.Type;
import org.jboss.unimbus.openapi.impl.api.models.ExternalDocumentationImpl;
import org.jboss.unimbus.openapi.impl.api.util.MergeUtil;
import org.jboss.unimbus.openapi.impl.OpenApiConstants;
import org.jboss.unimbus.openapi.impl.scanner.OpenApiDataObjectScanner;

import static org.jboss.unimbus.openapi.impl.OpenApiConstants.*;

/**
 * @author Marc Savy {@literal <marc@rhymewithgravy.com>}
 */
public class SchemaFactory {

    private SchemaFactory() {
    }

    @SuppressWarnings("unchecked")
    public static Schema readSchema(IndexView index,
                                    Schema schema,
                                    AnnotationInstance annotation,
                                    Map<String, Object> overrides) {
        if (annotation == null) {
            return schema;
        }

        // Schemas can be hidden. Skip if that's the case.
        Boolean isHidden = JandexUtil.booleanValue(annotation, PROP_HIDDEN);
        if (isHidden != null && isHidden == Boolean.TRUE) {
            return schema;
        }

        //schema.setDescription(JandexUtil.stringValue(annotation, ModelConstants.OpenApiConstants.PROP_DESCRIPTION));  IMPLEMENTATION
        schema.setNot((Schema) overrides.getOrDefault(PROP_NOT, readClassSchema(index, annotation.value(PROP_NOT))));
        schema.setOneOf((List<Schema>) overrides.getOrDefault(PROP_ONE_OF, readClassSchemas(index, annotation.value(PROP_ONE_OF))));
        schema.setAnyOf((List<Schema>) overrides.getOrDefault(PROP_ANY_OF, readClassSchemas(index, annotation.value(PROP_ANY_OF))));
        schema.setAllOf((List<Schema>) overrides.getOrDefault(PROP_ALL_OF, readClassSchemas(index, annotation.value(PROP_ALL_OF))));
        schema.setTitle((String) overrides.getOrDefault(PROP_TITLE, JandexUtil.stringValue(annotation, PROP_TITLE)));
        schema.setMultipleOf((BigDecimal) overrides.getOrDefault(PROP_MULTIPLE_OF, JandexUtil.bigDecimalValue(annotation, PROP_MULTIPLE_OF)));
        schema.setMaximum((BigDecimal) overrides.getOrDefault(PROP_MAXIMUM, JandexUtil.bigDecimalValue(annotation, PROP_MAXIMUM)));
        schema.setExclusiveMaximum((Boolean) overrides.getOrDefault(PROP_EXCLUSIVE_MAXIMUM, JandexUtil.booleanValue(annotation, PROP_EXCLUSIVE_MAXIMUM)));
        schema.setMinimum((BigDecimal) overrides.getOrDefault(PROP_MINIMUM, JandexUtil.bigDecimalValue(annotation, PROP_MINIMUM)));
        schema.setExclusiveMinimum((Boolean) overrides.getOrDefault(PROP_EXCLUSIVE_MINIMUM, JandexUtil.booleanValue(annotation, PROP_EXCLUSIVE_MINIMUM)));
        schema.setMaxLength((Integer) overrides.getOrDefault(PROP_MAX_LENGTH, JandexUtil.intValue(annotation, PROP_MAX_LENGTH)));
        schema.setMinLength((Integer) overrides.getOrDefault(PROP_MIN_LENGTH, JandexUtil.intValue(annotation, PROP_MIN_LENGTH)));
        schema.setPattern((String) overrides.getOrDefault(PROP_PATTERN, JandexUtil.stringValue(annotation, PROP_PATTERN)));
        schema.setMaxProperties((Integer) overrides.getOrDefault(PROP_MAX_PROPERTIES, JandexUtil.intValue(annotation, PROP_MAX_PROPERTIES)));
        schema.setMinProperties((Integer) overrides.getOrDefault(PROP_MIN_PROPERTIES, JandexUtil.intValue(annotation, PROP_MIN_PROPERTIES)));
        schema.setRequired((List<String>) overrides.getOrDefault(PROP_REQUIRED_PROPERTIES, JandexUtil.stringListValue(annotation, PROP_REQUIRED_PROPERTIES)));
        schema.setDescription((String) overrides.getOrDefault(PROP_DESCRIPTION, JandexUtil.stringValue(annotation, PROP_DESCRIPTION)));
        schema.setFormat((String) overrides.getOrDefault(PROP_FORMAT, JandexUtil.stringValue(annotation, PROP_FORMAT)));
        schema.setRef((String) overrides.getOrDefault(PROP_REF, JandexUtil.stringValue(annotation, PROP_REF)));
        schema.setNullable((Boolean) overrides.getOrDefault(PROP_NULLABLE, JandexUtil.booleanValue(annotation, PROP_NULLABLE)));
        schema.setReadOnly((Boolean) overrides.getOrDefault(PROP_READ_ONLY, JandexUtil.booleanValue(annotation, PROP_READ_ONLY)));
        schema.setWriteOnly((Boolean) overrides.getOrDefault(PROP_WRITE_ONLY, JandexUtil.booleanValue(annotation, PROP_WRITE_ONLY)));
        schema.setExample(overrides.getOrDefault(PROP_EXAMPLE, JandexUtil.stringValue(annotation, PROP_EXAMPLE)));
        schema.setExternalDocs(readExternalDocs(annotation.value(OpenApiConstants.PROP_EXTERNAL_DOCS)));
        schema.setDeprecated((Boolean) overrides.getOrDefault(PROP_DEPRECATED, JandexUtil.booleanValue(annotation, PROP_DEPRECATED)));
        schema.setType((Schema.SchemaType) overrides.getOrDefault(PROP_TYPE, JandexUtil.enumValue(annotation, PROP_TYPE, Schema.SchemaType.class)));
        schema.setEnumeration((List<Object>) overrides.getOrDefault(PROP_ENUM, JandexUtil.stringListValue(annotation, PROP_ENUM)));
        schema.setDefaultValue(overrides.getOrDefault(PROP_DEFAULT_VALUE, JandexUtil.stringValue(annotation, PROP_DEFAULT_VALUE)));
        //schema.setDiscriminator(readDiscriminatorMappings(annotation.value(OpenApiConstants.PROP_DISCRIMINATOR_MAPPING)));
        schema.setMaxItems((Integer) overrides.getOrDefault(PROP_MAX_ITEMS, JandexUtil.intValue(annotation, PROP_MAX_ITEMS)));
        schema.setMinItems((Integer) overrides.getOrDefault(PROP_MIN_ITEMS, JandexUtil.intValue(annotation, PROP_MIN_ITEMS)));
        schema.setUniqueItems((Boolean) overrides.getOrDefault(PROP_UNIQUE_ITEMS, JandexUtil.booleanValue(annotation, PROP_UNIQUE_ITEMS)));

        Schema implSchema = readClassSchema(index, annotation.value(PROP_IMPLEMENTATION));
        if (schema.getType() == Schema.SchemaType.ARRAY && implSchema != null) {
            // If the @Schema annotation indicates an array type, then use the Schema
            // generated from the implementation Class as the "items" for the array.
            schema.setItems(implSchema);
        } else {
            // If there is an impl class - merge the @Schema properties *onto* the schema
            // generated from the Class so that the annotation properties override the class
            // properties (as required by the MP+OAI spec).
            schema = MergeUtil.mergeObjects(implSchema, schema);
        }
        return schema;
    }

    /**
     * Introspect into the given Class to generate a Schema model.
     */
    private static Schema readClassSchema(IndexView index, AnnotationValue value) {
        if (value == null) {
            return null;
        }
        ClassType ctype = (ClassType) value.asClass();
        return introspectClassToSchema(index, ctype);
    }

    /**
     * Introspects the given class type to generate a Schema model.
     */
    private static Schema introspectClassToSchema(IndexView index, ClassType ctype) {
        return OpenApiDataObjectScanner.process(index, ctype);
    }

    private static List<Schema> readClassSchemas(IndexView index, AnnotationValue value) {
        if (value == null) {
            return null;
        }
        Type[] classArray = value.asClassArray();
        List<Schema> schemas = new ArrayList<>(classArray.length);
        for (Type type : classArray) {
            ClassType ctype = (ClassType) type;
            Schema schema = introspectClassToSchema(index, ctype);
            schemas.add(schema);
        }
        return schemas;
    }

    private static ExternalDocumentation readExternalDocs(AnnotationValue externalDocAnno) {
        if (externalDocAnno == null) {
            return null;
        }
        AnnotationInstance nested = externalDocAnno.asNested();
        ExternalDocumentation externalDoc = new ExternalDocumentationImpl();
        externalDoc.setDescription(JandexUtil.stringValue(nested, OpenApiConstants.PROP_DESCRIPTION));
        externalDoc.setUrl(JandexUtil.stringValue(nested, OpenApiConstants.PROP_URL));
        return externalDoc;
    }
}
