/**
 * Copyright 2018 Red Hat, Inc, and individual contributors.
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
package org.wildfly.swarm.microprofile.openapi.runtime;

import org.eclipse.microprofile.openapi.models.media.Schema;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.ClassType;
import org.jboss.jandex.DotName;
import org.jboss.jandex.FieldInfo;
import org.jboss.jandex.IndexView;
import org.jboss.jandex.Type;
import org.jboss.logging.Logger;
import org.wildfly.swarm.microprofile.openapi.OpenApiConstants;
import org.wildfly.swarm.microprofile.openapi.models.media.SchemaImpl;
import org.wildfly.swarm.microprofile.openapi.util.JandexUtil;
import org.wildfly.swarm.microprofile.openapi.util.SchemaFactory;
import org.wildfly.swarm.microprofile.openapi.util.TypeUtil;

import javax.validation.constraints.NotNull;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

/**
 * Attempts to convert class graph into {@link Schema}.
 *
 * Searches depth first.
 *
 * @author Marc Savy {@literal <marc@rhymewithgravy.com>}
 */
public class OpenApiDataObjectScanner {

    private static final Logger LOG = Logger.getLogger("org.wildfly.swarm.microprofile.openapi");
    private final IndexView index;
    private final ClassType rootClassType;
    private final ClassInfo rootClassInfo;
    private final SchemaImpl rootSchema;
    private final TypeUtil.TypeWithFormat classTypeFormat;

    public OpenApiDataObjectScanner(IndexView index, ClassType classType) {
        this.index = index;
        this.rootClassType = classType;
        this.rootClassInfo = index.getClassByName(classType.name());
        this.classTypeFormat = TypeUtil.getTypeFormat(classType);
        this.rootSchema = new SchemaImpl();
    }

    private boolean isTerminalType(ClassType classType) {
        TypeUtil.TypeWithFormat tf = TypeUtil.getTypeFormat(classType);
        return tf.getSchemaType() != Schema.SchemaType.OBJECT &&
                tf.getSchemaType() != Schema.SchemaType.ARRAY;
    }

    public static Schema process(IndexView index, ClassType classType) {
        return new OpenApiDataObjectScanner(index, classType).process();
    }

    public Schema process() {
        // If top level item is simple
        if (isTerminalType(rootClassType)) {
            SchemaImpl simpleSchema = new SchemaImpl();
            simpleSchema.setType(classTypeFormat.getSchemaType());
            simpleSchema.setFormat(classTypeFormat.getFormat().format());
            return simpleSchema;
        }

        // If top level item is not indexed
        if (rootClassInfo == null) {
            return null;
        }

        dfs(rootClassInfo);
        return rootSchema;
    }

    // Scan depth first.
    private void dfs(ClassInfo classInfo) {
        ClassInfo currentClass = classInfo;
        SchemaImpl currentSchema = rootSchema;
        Deque<PathEntry> path = new ArrayDeque<>();
        path.push(new PathEntry(currentClass, currentSchema));

        while (!path.isEmpty()) {
            // First, handle class annotations.
            readKlass(currentClass, currentSchema, path);

            // Handle fields
            for (FieldInfo field : currentClass.fields()) {
                processField(field, currentSchema, path);
            }

            // Handle methods
            // TODO put it here!

            PathEntry pair = path.pop();
            currentClass = pair.getClazz();
            currentSchema = pair.getSchema();
        }
    }

    private Schema readKlass(ClassInfo currentClass,
                             SchemaImpl currentSchema,
                             Deque<PathEntry> path) {
        AnnotationInstance annotation = getSchemaAnnotation(currentClass);
        if (annotation != null) {
            // Because of implementation= field, *may* return a new schema rather than modify.
            return SchemaFactory.readSchema(currentSchema, annotation, Collections.emptyMap());
        }
        return currentSchema;
    }

    private Schema processField(FieldInfo field, SchemaImpl parentSchema, Deque<PathEntry> path) {
        SchemaImpl fieldSchema = new SchemaImpl();
        // Is simple property
        parentSchema.addProperty(field.name(), fieldSchema);

        // TODO Is an array type, etc.
        //fieldSchema.items()

        AnnotationInstance schemaAnno = getSchemaAnnotation(field);

        if (schemaAnno != null) {
            // 1. Handle field annotated with @Schema.
            readSchemaAnnotatedField(schemaAnno, field, parentSchema, fieldSchema, path);
        } else {
            // 2. Handle unannotated field and just do simple inference.
            readUnannotatedField(field, fieldSchema, path);
        }

        return fieldSchema;
    }

    private AnnotationInstance getSchemaAnnotation(ClassInfo field) {
        return getAnnotation(field, OpenApiConstants.DOTNAME_SCHEMA);
    }

    private AnnotationInstance getSchemaAnnotation(FieldInfo field) {
        return getAnnotation(field, OpenApiConstants.DOTNAME_SCHEMA);

    }

    private AnnotationInstance getAnnotation(ClassInfo field, DotName annotationName) {
        return field.classAnnotations().stream()
                .filter(annotation -> annotation.name().equals(annotationName))
                .findFirst()
                .orElse(null);
    }

    private AnnotationInstance getAnnotation(FieldInfo field, DotName annotationName) {
        return field.annotations().stream()
                .filter(annotation -> annotation.name().equals(annotationName))
                .findFirst()
                .orElse(null);
    }

    private void readUnannotatedField(FieldInfo fieldInfo,
                                      SchemaImpl schema,
                                      Deque<PathEntry> path) {
        LOG.debug("Processing an unannotated field. May attempt to infer type information.");

        if (!shouldInferUnannotatedFields()) {
            return;
        }

        TypeUtil.TypeWithFormat typeFormat = inferFieldTypeFormat(fieldInfo);
        schema.setType(typeFormat.getSchemaType());

        if (typeFormat.getFormat().hasFormat()) {
            schema.setFormat(typeFormat.getFormat().format());
        }

        pushFieldToPath(fieldInfo, schema, path, typeFormat);
    }

    private void pushFieldToPath(FieldInfo fieldInfo, SchemaImpl schema, Deque<PathEntry> path, TypeUtil.TypeWithFormat typeFormat) {
        if (typeFormat.getSchemaType() == Schema.SchemaType.OBJECT) {
            ClassType klazzType = fieldInfo.type().asClassType();
            ClassInfo klazzInfo = index.getClassByName(klazzType.name());
            pushPathPair(path, klazzInfo, schema);
        } else if (typeFormat.getSchemaType() == Schema.SchemaType.ARRAY) {
            System.out.println("it's an array; what do?");
            //TODO schema.addProperties
        }
    }

    private void pushPathPair(Deque<PathEntry> path, ClassInfo klazzInfo, SchemaImpl schema) {
        PathEntry pair = new PathEntry(klazzInfo, schema);
        if (path.contains(pair)) {
            // Cycle detected, don't push path. TODO could be interestingto use reference?
            LOG.infov("Possible cycle was detected in {0}. Will not search further.", klazzInfo);
            LOG.debugv("Path stack: {0}", path);
        } else {
            // Push path to be inspected later.
            LOG.debugv("Adding child node to path: {0}", klazzInfo);
            path.push(pair);
        }
    }

    private Schema readSchemaAnnotatedField(AnnotationInstance annotation,
                                            FieldInfo fieldInfo,
                                            SchemaImpl parent,
                                            SchemaImpl schema,
                                            Deque<PathEntry> path) {
        if (annotation == null) {
            return parent;
        }

        LOG.debug("Processing @Schema annotation on a field.");

        // Schemas can be hidden. Skip if that's the case.
        Boolean isHidden = JandexUtil.booleanValue(annotation, OpenApiConstants.PROP_HIDDEN);
        if (isHidden != null && isHidden == Boolean.TRUE) {
            return parent;
        }

        // If "required" attribute is on field. It should be applied to the *parent* schema.
        // Required is false by default.
        if (JandexUtil.booleanValueWithDefault(annotation, OpenApiConstants.PROP_REQUIRED)) {
            parent.addRequired(fieldInfo.name());
        }

        // TypeFormat pair contains mappings for Java <-> OAS types and formats.
        TypeUtil.TypeWithFormat typeFormat = inferFieldTypeFormat(fieldInfo);
        Map<String, Object> overrides = new HashMap<>();
        overrides.put(OpenApiConstants.PROP_TYPE, typeFormat.getSchemaType());
        overrides.put(OpenApiConstants.PROP_FORMAT, typeFormat.getFormat().format());

        // Field may need searching further.
        pushFieldToPath(fieldInfo, schema, path, typeFormat);
        return SchemaFactory.readSchema(schema, annotation, overrides);
    }

    // This may be an array, a primitive, or a generic type definition.
    private TypeUtil.TypeWithFormat inferFieldTypeFormat(@NotNull FieldInfo field) {
        Type fieldType = field.type();
        switch (fieldType.kind()) {
            case CLASS:
                return TypeUtil.getTypeFormat(fieldType.asClassType());
            case PRIMITIVE:
                return TypeUtil.getTypeFormat(fieldType.asPrimitiveType());
            case ARRAY:
                return TypeUtil.getTypeFormat(fieldType.asArrayType());
            case VOID:
                break;
            case TYPE_VARIABLE: // TODO
                break;
            case UNRESOLVED_TYPE_VARIABLE: // TODO
                break;
            case WILDCARD_TYPE: // TODO
                break;
            case PARAMETERIZED_TYPE: // TODO
                break;
            default:
                throw new IllegalStateException("Unexpected kind for " + field + ": " + fieldType.kind());
        }
        throw new IllegalStateException();
    }

    private boolean shouldInferUnannotatedFields() {
        String infer = System.getProperties().getProperty("openapi.infer-unannotated-types", "true");
        return Boolean.parseBoolean(infer); // TODO is there some standard for this?
    }

    // Needed for non-recursive DFS to keep schema and class together.
    private static final class PathEntry {
        private final ClassInfo clazz;
        private final SchemaImpl schema;

        PathEntry(ClassInfo clazz, SchemaImpl schema) {
            this.clazz = clazz;
            this.schema = schema;
        }

        ClassInfo getClazz() {
            return clazz;
        }

        SchemaImpl getSchema() {
            return schema;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            PathEntry pair = (PathEntry) o;

            return clazz != null ? clazz.equals(pair.clazz) : pair.clazz == null;
        }

        @Override
        public int hashCode() {
            return clazz != null ? clazz.hashCode() : 0;
        }

        @Override
        public String toString() {
            return "Pair{" +
                    "clazz=" + clazz +
                    ", schema=" + schema +
                    '}';
        }
    }

}
