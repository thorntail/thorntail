/*
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
import org.jboss.jandex.ParameterizedType;
import org.jboss.jandex.Type;
import org.jboss.logging.Logger;
import org.wildfly.swarm.microprofile.openapi.OpenApiConstants;
import org.wildfly.swarm.microprofile.openapi.models.media.SchemaImpl;
import org.wildfly.swarm.microprofile.openapi.util.JandexUtil;
import org.wildfly.swarm.microprofile.openapi.util.SchemaFactory;
import org.wildfly.swarm.microprofile.openapi.util.TypeUtil;

import javax.validation.constraints.NotNull;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
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

    // Collection (list-type things)
    private static final DotName COLLECTION_INTERFACE_NAME = DotName.createSimple(Collection.class.getName());
    private static final Type COLLECTION_TYPE = Type.create(COLLECTION_INTERFACE_NAME, Type.Kind.CLASS);

    // Map
    private static final DotName MAP_INTERFACE_NAME = DotName.createSimple(Map.class.getName());
    private static final Type MAP_TYPE = Type.create(MAP_INTERFACE_NAME, Type.Kind.CLASS);

    private final IndexView index;
    private final ClassType rootClassType;
    private final ClassInfo rootClassInfo;
    private final SchemaImpl rootSchema;
    private final TypeUtil.TypeWithFormat classTypeFormat;

    private ArrayDeque<PathEntry> path;

    public OpenApiDataObjectScanner(IndexView index, ClassType classType) {
        this.index = index;
        this.rootClassType = classType;
        this.rootClassInfo = index.getClassByName(classType.name());
        this.classTypeFormat = TypeUtil.getTypeFormat(classType);
        this.rootSchema = new SchemaImpl();
    }

    private boolean isTerminalType(Type classType) {
        TypeUtil.TypeWithFormat tf = TypeUtil.getTypeFormat(classType);
        return tf.getSchemaType() != Schema.SchemaType.OBJECT &&
                tf.getSchemaType() != Schema.SchemaType.ARRAY;
    }

    public static Schema process(IndexView index, ClassType classType) {
        return new OpenApiDataObjectScanner(index, classType).process();
    }

    public Schema process() {

        System.out.println("Processing class: " + this.rootClassType.name());
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
    private void dfs(@NotNull ClassInfo classInfo) {
        PathEntry currentPathEntry = new PathEntry(classInfo, rootSchema);
        path = new ArrayDeque<>();
        path.push(currentPathEntry);

        while (!path.isEmpty()) {
            currentPathEntry = path.pop();
            ClassInfo currentClass = currentPathEntry.clazz;
            SchemaImpl currentSchema = currentPathEntry.schema;

            // First, handle class annotations.
            currentSchema = readKlass(currentClass, currentSchema);

            // Get all fields *including* inherited.
            List<FieldInfo> allFields = TypeUtil.getAllFields(index, currentClass);

            // Handle fields
            for (FieldInfo field : allFields) {
                processField(field, currentSchema, currentPathEntry);
            }

            // Handle methods
            // TODO put it here!
        }
    }

    private SchemaImpl readKlass(ClassInfo currentClass,
                             SchemaImpl currentSchema) {
        AnnotationInstance annotation = getSchemaAnnotation(currentClass);
        if (annotation != null) {
            // Because of implementation= field, *may* return a new schema rather than modify.
            return SchemaFactory.readSchema(currentSchema, annotation, Collections.emptyMap());
        }
        return currentSchema;
    }

    private Schema processField(FieldInfo field, SchemaImpl parentSchema, PathEntry currentPathEntry) {
        SchemaImpl fieldSchema = new SchemaImpl();
        // Is simple property
        parentSchema.addProperty(field.name(), fieldSchema);

        // TODO Is an array type, etc?
        //fieldSchema.items()

        AnnotationInstance schemaAnno = getSchemaAnnotation(field);

        if (schemaAnno != null) {
            // 1. Handle field annotated with @Schema.
            return readSchemaAnnotatedField(schemaAnno, field, parentSchema, fieldSchema, currentPathEntry);
        } else {
            // 2. Handle unannotated field and just do simple inference.
            readUnannotatedField(field, fieldSchema, currentPathEntry);
            // Unannotated won't result in substitution, so just return field schema.
            return fieldSchema;
        }
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

    private void pushFieldToPath(FieldInfo fieldInfo,
                                 SchemaImpl schema) {
            ClassType klazzType = fieldInfo.type().asClassType();
            ClassInfo klazzInfo = index.getClassByName(klazzType.name());
            pushPathPair(klazzInfo, schema);
    }

    private void pushPathPair(@NotNull ClassInfo klazzInfo,
                              @NotNull SchemaImpl schema) {

        PathEntry pair = new PathEntry(klazzInfo, schema);

//    // FIXME cycle detection not quite right.
//    if (path.contains(pair)) {
//            // Cycle detected, don't push path. TODO could be interesting to use reference?
//            LOG.infov("Possible cycle was detected in {0}. Will not search further.", klazzInfo);
//            LOG.debugv("Path stack: {0}", path);
//        } else {
//            // Push path to be inspected later.
//            LOG.debugv("Adding child node to path: {0}", klazzInfo);
//            path.push(pair);
//        }
        path.push(pair);
    }

    private Schema readSchemaAnnotatedField(AnnotationInstance annotation,
                                            FieldInfo fieldInfo,
                                            SchemaImpl parent,
                                            SchemaImpl schema,
                                            PathEntry pathEntry) {
        if (annotation == null) {
            return parent;
        }

        LOG.debugv("Processing @Schema annotation {0} on a field {1}", annotation, fieldInfo);

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

        // Type could be replaced (e.g. generics).
        Type postProcessedField = processType(fieldInfo, schema, pathEntry);

        // TypeFormat pair contains mappings for Java <-> OAS types and formats.
        TypeUtil.TypeWithFormat typeFormat = TypeUtil.getTypeFormat(postProcessedField);

        // Provide inferred type and format if relevant.
        Map<String, Object> overrides = new HashMap<>();
        overrides.put(OpenApiConstants.PROP_TYPE, typeFormat.getSchemaType());
        overrides.put(OpenApiConstants.PROP_FORMAT, typeFormat.getFormat().format());
        return SchemaFactory.readSchema(schema, annotation, overrides);
    }

    private Type processType(FieldInfo fieldInfo, SchemaImpl schema, PathEntry pathEntry) {

        // If it's a terminal type.
        if (isTerminalType(fieldInfo.type())) {
            return fieldInfo.type();
        }

        if (fieldInfo.type().kind() == Type.Kind.PARAMETERIZED_TYPE) {
            // Parameterised type (e.g. Foo<A, B>)
            readParamType(schema, fieldInfo.type().asParameterizedType());
            return fieldInfo.type();
        } else if (fieldInfo.type().kind() == Type.Kind.ARRAY) {
//            // TODO treat as list
//            throw new UnsupportedOperationException("array support needs implementing not yet supported.");
            return fieldInfo.type();
        } else if (fieldInfo.type().kind() == Type.Kind.TYPE_VARIABLE) {
            // Type variable (e.g. A in List<A>)
            Type resolvedType = pathEntry.resolvedTypes.pop();
            LOG.debugv("Resolved type {0} -> {1}", fieldInfo, resolvedType);
            if (isTerminalType(resolvedType)) {
                TypeUtil.TypeWithFormat replacement = TypeUtil.getTypeFormat(resolvedType);
                schema.setType(replacement.getSchemaType());
                schema.setFormat(replacement.getFormat().format());
            } else {
                ClassInfo klazz = index.getClassByName(resolvedType.name());
                LOG.debugv("Attempting to do TYPE_VARIABLE substitution: {0} -> {1}", fieldInfo, resolvedType);
                pushPathPair(klazz, schema);
            }
            return resolvedType;
        } else {
            // Simple case: bare class or primitive type.
            pushFieldToPath(fieldInfo, schema);
            return fieldInfo.type();
        }
    }

    private void readUnannotatedField(FieldInfo fieldInfo,
                                      SchemaImpl schema,
                                      PathEntry pathEntry) {
        if (!shouldInferUnannotatedFields()) {
            return;
        }

        LOG.debugv("Processing unannotated field {0}.", fieldInfo);

        TypeUtil.TypeWithFormat typeFormat = inferFieldTypeFormat(fieldInfo);
        schema.setType(typeFormat.getSchemaType());

        if (typeFormat.getFormat().hasFormat()) {
            schema.setFormat(typeFormat.getFormat().format());
        }

        processType(fieldInfo, schema, pathEntry);
    }

    private boolean isA(Type testSubject, Type test) {
        return TypeUtil.isA(index, testSubject, test);
    }

    private void readParamType(SchemaImpl schema, ParameterizedType pType) {
        LOG.debugv("Processing parameterized type {0}", pType);

        // If it's a collection, we should treat it as an array.
        if (isA(pType, COLLECTION_TYPE)) { // TODO maybe also Iterable?
            LOG.debugv("Found a Java Collection. Will treat as an array.");
            SchemaImpl arraySchema = new SchemaImpl();
            schema.type(Schema.SchemaType.ARRAY);
            schema.items(arraySchema);

            // E.g. In Foo<A, B> this will be: A, B
            for (Type argument : pType.arguments()) {
                // FIXME this won't work for non-Jandex types -- if we do this we'll need to jump into pure reflection.
                if (isTerminalType(argument)) {
                    TypeUtil.TypeWithFormat terminalType = TypeUtil.getTypeFormat(argument);
                    arraySchema.type(terminalType.getSchemaType());
                    arraySchema.format(terminalType.getFormat().format());
                } else {
                    ClassInfo klazz = index.getClassByName(argument.name());
                    pushPathPair(klazz, arraySchema);
                }
            }
        } else if (isA(pType, MAP_TYPE)) {
            LOG.debugv("Found a map. Will treat as an object.");
            schema.type(Schema.SchemaType.OBJECT);

            if (pType.arguments().size() == 2) {
                Type valueType = pType.arguments().get(1);
                SchemaImpl propsSchema = new SchemaImpl();
                if (isTerminalType(valueType)) {
                    TypeUtil.TypeWithFormat tf = TypeUtil.getTypeFormat(valueType);
                    propsSchema.setType(tf.getSchemaType());
                    propsSchema.setFormat(tf.getFormat().format());
                } else {
                    ClassInfo klazz = index.getClassByName(valueType.name());
                    pushPathPair(klazz, propsSchema);
                }
                schema.additionalProperties(propsSchema);
            }
        } else {
            // This attempts to allow us to resolve the types issue.
            ClassInfo klazz = index.getClassByName(pType.name());
            PathEntry pair = new PathEntry(klazz, schema, pType.arguments());
            path.push(pair);
        }
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
            case TYPE_VARIABLE:
                return TypeUtil.objectFormat();
            case UNRESOLVED_TYPE_VARIABLE: // TODO
                break;
            case WILDCARD_TYPE: // TODO
                break;
            case PARAMETERIZED_TYPE:
                return TypeUtil.objectFormat();
            default:
                throw new IllegalStateException("Unhandled kind " + fieldType.kind());
        }
        throw new IllegalStateException("Unexpected kind for " + field + ": " + fieldType.kind());
    }

    private boolean shouldInferUnannotatedFields() {
        String infer = System.getProperties().getProperty("openapi.infer-unannotated-types", "true");
        return Boolean.parseBoolean(infer); // TODO: is there some standard for this?
    }

    // Needed for non-recursive DFS to keep schema and class together.
    private static final class PathEntry {
        private final ClassInfo clazz;
        private final SchemaImpl schema;

        // Generic args to class pushed on stack so we can resolve the fields?
        private final Deque<Type> resolvedTypes = new ArrayDeque<>();

        PathEntry(ClassInfo clazz, SchemaImpl schema) {
            this.clazz = clazz;
            this.schema = schema;
        }

        @SuppressWarnings("unused")
        PathEntry(ClassInfo clazz, SchemaImpl schema, Type... types) {
            this.clazz = clazz;
            this.schema = schema;
            this.resolvedTypes.addAll(Arrays.asList(types));
        }

        PathEntry(ClassInfo clazz, SchemaImpl schema, List<Type> types) {
            this.clazz = clazz;
            this.schema = schema;
            this.resolvedTypes.addAll(types);
        }

        @SuppressWarnings("unused")
        ClassInfo getClazz() {
            return clazz;
        }

        @SuppressWarnings("unused")
        SchemaImpl getSchema() {
            return schema;
        }

        @SuppressWarnings("unused")
        Deque<Type> getResolvedTypes() {
            return resolvedTypes;
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
