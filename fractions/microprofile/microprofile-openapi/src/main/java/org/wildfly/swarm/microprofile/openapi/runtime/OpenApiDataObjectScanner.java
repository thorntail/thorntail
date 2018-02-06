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
import org.jboss.jandex.ArrayType;
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
    // Enum
    private static final DotName ENUM_INTERFACE_NAME = DotName.createSimple(Enum.class.getName());
    private static final Type ENUM_TYPE = Type.create(ENUM_INTERFACE_NAME, Type.Kind.CLASS);
    // String type
    private static final Type STRING_TYPE = Type.create(DotName.createSimple(String.class.getName()), Type.Kind.CLASS);


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
        LOG.debugv("Starting processing with root class: {0}", rootClassType.name());

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
        this.path = new ArrayDeque<>();
        PathEntry currentPathEntry = PathEntry.rootNode(classInfo, rootSchema);
        path.push(currentPathEntry);

        while (!path.isEmpty()) {
            ClassInfo currentClass = currentPathEntry.clazz;
            SchemaImpl currentSchema = currentPathEntry.schema;

            // First, handle class annotations.
            currentSchema = readKlass(currentClass, currentSchema);

            // Get all fields *including* inherited.
            List<FieldInfo> allFields = TypeUtil.getAllFields(index, currentClass);

            // Handle fields
            for (FieldInfo field : allFields) {
                LOG.tracev("Iterating field {0}", field);
                processField(field, currentSchema, currentPathEntry);
            }

            currentPathEntry = path.pop();
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

    private SchemaImpl processField(FieldInfo field, SchemaImpl parentSchema, PathEntry currentPathEntry) {
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

    private void pushFieldToPath(PathEntry parentPathEntry,
                                 FieldInfo fieldInfo,
                                 SchemaImpl schema) {
            ClassType klazzType = fieldInfo.type().asClassType();
            ClassInfo klazzInfo = index.getClassByName(klazzType.name());
            pushPathPair(parentPathEntry, klazzInfo, schema);
    }

    private void pushPathPair(@NotNull PathEntry parentPathEntry,
                              @NotNull ClassInfo klazzInfo,
                              @NotNull SchemaImpl schema) {
        PathEntry entry = PathEntry.leafNode(parentPathEntry, klazzInfo, schema);
        if (parentPathEntry.hasParent(entry)) {
            // Cycle detected, don't push path. TODO could be interesting to use reference?
            LOG.debugv("Possible cycle was detected in: {0}. Will not search further.", klazzInfo);
            LOG.tracev("Path: {0}", entry.toStringWithGraph());
        } else {
            // Push path to be inspected later.
            LOG.debugv("Adding child node to path: {0}", klazzInfo);
            path.push(entry);
        }
    }

    private SchemaImpl readSchemaAnnotatedField(AnnotationInstance annotation,
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

    private ClassInfo getClassByName(Type type) {
        return index.getClassByName(TypeUtil.getName(type));
    }

    private Type processType(FieldInfo fieldInfo, SchemaImpl schema, PathEntry pathEntry) {

        // If it's a terminal type.
        if (isTerminalType(fieldInfo.type())) {
            return fieldInfo.type();
        }

        if (isA(fieldInfo.type(), ENUM_TYPE)) {
            LOG.debugv("Processing an enum {0}", fieldInfo);
            ClassInfo enumKlazz = getClassByName(fieldInfo.type());

            for (FieldInfo enumField : enumKlazz.fields()) {
                // Ignore the hidden enum array as it's not accessible. Add fields that look like enums (of type enumKlazz)
                if (!enumField.name().equals("$VALUES") && TypeUtil.getName(enumField.type()).equals(enumKlazz.name())) {
                    // Enum's value fields.
                    schema.addEnumeration(enumField.name());
                }
            }
            return STRING_TYPE;
        }

        if (fieldInfo.type().kind() == Type.Kind.PARAMETERIZED_TYPE) {
            // Parameterised type (e.g. Foo<A, B>)
            readParamType(pathEntry, schema, fieldInfo.type().asParameterizedType());
            return fieldInfo.type();
        } else if (fieldInfo.type().kind() == Type.Kind.ARRAY) {
            LOG.debugv("Processing an array {0}", fieldInfo);
            ArrayType arrayType = fieldInfo.type().asArrayType();
            // TODO handle multi-dimensional arrays.
            schema.type(Schema.SchemaType.ARRAY);
            SchemaImpl arrSchema = new SchemaImpl();

            TypeUtil.TypeWithFormat typeFormat = TypeUtil.getTypeFormat(arrayType.component());
            arrSchema.setType(typeFormat.getSchemaType());
            arrSchema.setFormat(typeFormat.getFormat().format());

            if (!isTerminalType(arrayType.component())) {
                ClassInfo klazz = getClassByName(fieldInfo.type());
                pushPathPair(pathEntry, klazz, arrSchema);
            }
            schema.items(arrSchema);
            return fieldInfo.type();
        } else if (fieldInfo.type().kind() == Type.Kind.TYPE_VARIABLE) {
            // Type variable (e.g. A in List<A>)
            Type resolvedType = pathEntry.resolvedTypes.pop();
            LOG.debugv("Resolved type {0} -> {1}", fieldInfo, resolvedType);
            if (isTerminalType(resolvedType)) {
                LOG.tracev("Is a terminal type");
                TypeUtil.TypeWithFormat replacement = TypeUtil.getTypeFormat(resolvedType);
                schema.setType(replacement.getSchemaType());
                schema.setFormat(replacement.getFormat().format());
            } else {
                ClassInfo klazz = index.getClassByName(resolvedType.name());
                LOG.debugv("Attempting to do TYPE_VARIABLE substitution: {0} -> {1}", fieldInfo, resolvedType);
                pushPathPair(pathEntry, klazz, schema);
            }
            return resolvedType;
        } else {
            // Simple case: bare class or primitive type.
            pushFieldToPath(pathEntry, fieldInfo, schema);
            return fieldInfo.type();
        }
    }

    private void readUnannotatedField(FieldInfo fieldInfo,
                                      SchemaImpl schema,
                                      PathEntry pathEntry) {
        if (!shouldInferUnannotatedFields()) {
            return;
        }

        LOG.debugv("Processing unannotated field {0}", fieldInfo);

        Type processedType = processType(fieldInfo, schema, pathEntry);

        TypeUtil.TypeWithFormat typeFormat = TypeUtil.getTypeFormat(processedType);
        schema.setType(typeFormat.getSchemaType());

        if (typeFormat.getFormat().hasFormat()) {
            schema.setFormat(typeFormat.getFormat().format());
        }

    }

    private boolean isA(Type testSubject, Type test) {
        return TypeUtil.isA(index, testSubject, test);
    }

    private void readParamType(PathEntry pathEntry, SchemaImpl schema, ParameterizedType pType) {
        LOG.debugv("Processing parameterized type {0}", pType);

        // If it's a collection, we should treat it as an array.
        if (isA(pType, COLLECTION_TYPE)) { // TODO maybe also Iterable?
            LOG.debugv("Processing Java Collection. Will treat as an array.");
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
                    pushPathPair(pathEntry, klazz, arraySchema);
                }
            }
        } else if (isA(pType, MAP_TYPE)) {
            LOG.debugv("Processing Map. Will treat as an object.");
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
                    pushPathPair(pathEntry, klazz, propsSchema);
                }
                schema.additionalProperties(propsSchema);
            }
        } else {
            // This attempts to allow us to resolve the types issue.
            ClassInfo klazz = index.getClassByName(pType.name());
            PathEntry pair = new PathEntry(pathEntry, klazz, schema, pType.arguments());
            path.push(pair);
        }
    }

    private boolean shouldInferUnannotatedFields() {
        String infer = System.getProperties().getProperty("openapi.infer-unannotated-types", "true");
        return Boolean.parseBoolean(infer); // TODO: is there some standard for this?
    }

    // Needed for non-recursive DFS to keep schema and class together.
    @SuppressWarnings("unused")
    private static final class PathEntry {
        private final PathEntry parent;
        private final ClassInfo clazz;
        private final SchemaImpl schema;
        // Generic args to class pushed on stack so we can resolve the fields?
        private final Deque<Type> resolvedTypes = new ArrayDeque<>();

        PathEntry(PathEntry parent, ClassInfo clazz, SchemaImpl schema) {
            this.parent = parent;
            this.clazz = clazz;
            this.schema = schema;
        }

        PathEntry(PathEntry parent, ClassInfo clazz, SchemaImpl schema, Type... types) {
            this.parent = parent;
            this.clazz = clazz;
            this.schema = schema;
            this.resolvedTypes.addAll(Arrays.asList(types));
        }

        PathEntry(PathEntry parent, ClassInfo clazz, SchemaImpl schema, List<Type> types) {
            this.parent = parent;
            this.clazz = clazz;
            this.schema = schema;
            this.resolvedTypes.addAll(types);
        }

        ClassInfo getClazz() {
            return clazz;
        }

        SchemaImpl getSchema() {
            return schema;
        }

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

        String toStringWithGraph() {
            return "Pair{" +
                    "clazz=" + clazz +
                    ", schema=" + schema +
                    ", parent=" + (parent != null ? parent.toStringWithGraph() : "<root>") + "}";
        }

        boolean hasParent(PathEntry candidate) {
            PathEntry test = this;
            while (test != null) {
                if (candidate.equals(test)) {
                    return true;
                }
              test = test.parent;
            }
            return false;
        }

        static PathEntry rootNode(ClassInfo classInfo, SchemaImpl rootSchema) {
            return new PathEntry(null, classInfo, rootSchema);
        }

        static PathEntry leafNode(PathEntry parentNode, ClassInfo classInfo, SchemaImpl rootSchema) {
            return new PathEntry(parentNode, classInfo, rootSchema);
        }
    }

}
