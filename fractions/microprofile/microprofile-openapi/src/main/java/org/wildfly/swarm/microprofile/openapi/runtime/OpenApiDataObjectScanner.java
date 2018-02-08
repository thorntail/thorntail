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
import org.jboss.jandex.TypeVariable;
import org.jboss.jandex.WildcardType;
import org.jboss.logging.Logger;
import org.wildfly.swarm.microprofile.openapi.api.models.media.SchemaImpl;
import org.wildfly.swarm.microprofile.openapi.runtime.util.JandexUtil;
import org.wildfly.swarm.microprofile.openapi.runtime.util.SchemaFactory;
import org.wildfly.swarm.microprofile.openapi.runtime.util.TypeUtil;

import javax.validation.constraints.NotNull;
import java.lang.reflect.Modifier;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Explores the class graph from the provided root, creating an OpenAPI {@link Schema}
 * from the entities encountered.
 *<p>
 * A depth first search is performed, with the following precedence (high to low):
 * <ol>
 *   <li>Explicitly provided attributes/overrides on <tt>@Schema</tt> annotated elements.
 *       Note that some attributes have special behaviours: for example, <tt>ref</tt> is mutually
 *       exclusive, and <tt>implementation</tt> replaces the implementation entirely.</li>
 *   <li>Unannotated fields unless property <tt>openapi.infer-unannotated-types</tt> set false</li>
 *   <li>Inferred attributes, such as name, type, format, etc.</li>
 * </ol>
 *
 * <p>
 * Well-known types, such as Collection, Map, Date, etc, are handled in a custom manner.
 * Jandex-indexed objects from the user's deployment are traversed until a terminal type is
 * met (such as a primitive, boxed primitive, date, etc), or an entity is encountered that is not
 * well-known or is not in the Jandex {@link IndexView}.
 *
 * <em>Current Limitations:</em>
 * If a type is not available in the provided IndexView then it is not accessible. Excepting
 * well-known types, this means non-deployment objects may not be scanned.
 * <p>
 * Future work could consider making the user's deployment classes available to this classloader,
 * with additional code to traverse non-Jandex types reachable from this classloader. But, this is
 * troublesome for performance, security and initialisation reasons -- particular caution would
 * be needed to avoid accidental initialisation of classes that may have externally visible side-effects.
 *
 * @see org.eclipse.microprofile.openapi.annotations.media.Schema Schema Annotation
 * @see Schema Schema Object
 * @author Marc Savy {@literal <marc@rhymewithgravy.com>}
 */
public class OpenApiDataObjectScanner {

    private static final Logger LOG = Logger.getLogger(OpenApiDataObjectScanner.class);
    // Object
    private static final Type OBJECT_TYPE = Type.create(DotName.createSimple(java.lang.Object.class.getName()), Type.Kind.CLASS);
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
    // Array type
    private static final Type ARRAY_TYPE_OBJECT = ArrayType.create(DotName.createSimple("[Ljava.lang.Object;"), Type.Kind.ARRAY);

    private final IndexView index;
    private final ClassType rootClassType;
    private final ClassInfo rootClassInfo;
    private final SchemaImpl rootSchema;
    private final TypeUtil.TypeWithFormat classTypeFormat;
    private final Deque<PathEntry> path = new ArrayDeque<>();

    /**
     * Constructor for data object scanner.
     *
     * Call {@link #process()} to build and return the {@link Schema}.
     *
     * @param index index of types to scan
     * @param classType root to begin scan
     */
    public OpenApiDataObjectScanner(IndexView index, ClassType classType) {
        this.index = index;
        this.rootClassType = classType;
        this.rootClassInfo = getClassByName(classType);
        this.classTypeFormat = TypeUtil.getTypeFormat(classType);
        this.rootSchema = new SchemaImpl();
    }

    /**
     * Build a Schema with classType as root.
     *
     * @param index index of types to scan
     * @param classType root to begin scan
     * @return the OAI schema
     */
    public static Schema process(IndexView index, ClassType classType) {
        return new OpenApiDataObjectScanner(index, classType).process();
    }

    /**
     * Build the Schema
     *
     * @return the OAI schema
     */
    public Schema process() {
        LOG.debugv("Starting processing with root: {0}", rootClassType.name());

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
        PathEntry currentPathEntry = PathEntry.rootNode(classInfo, rootSchema);
        path.push(currentPathEntry);

        while (!path.isEmpty()) {
            ClassInfo currentClass = currentPathEntry.clazz;
            Schema currentSchema = currentPathEntry.schema;

            // First, handle class annotations.
            currentSchema = readKlass(currentClass, currentSchema);

            // Get all fields *including* inherited.
            List<FieldInfo> allFields = TypeUtil.getAllFields(index, currentClass);

            // Handle fields
            for (FieldInfo field : allFields) {
                if (!Modifier.isStatic(field.flags())) {
                    LOG.tracev("Iterating field {0}", field);
                    processField(field, currentSchema, currentPathEntry);
                }
            }
            currentPathEntry = path.pop();
            // Handle methods
            // TODO put it here!
        }
    }

    private Schema readKlass(ClassInfo currentClass,
                             Schema currentSchema) {
        AnnotationInstance annotation = getSchemaAnnotation(currentClass);
        if (annotation != null) {
            // Because of implementation= field, *may* return a new schema rather than modify.
            return SchemaFactory.readSchema(index, currentSchema, annotation, Collections.emptyMap());
        }
        return currentSchema;
    }

    private Schema processField(FieldInfo field, Schema parentSchema, PathEntry currentPathEntry) {
        Schema fieldSchema = new SchemaImpl();
        parentSchema.addProperty(field.name(), fieldSchema);

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

    private Schema readSchemaAnnotatedField(AnnotationInstance annotation,
                                            FieldInfo fieldInfo,
                                            Schema parent,
                                            Schema schema,
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
        return SchemaFactory.readSchema(index, schema, annotation, overrides);
    }

    private Type processType(FieldInfo fieldInfo, Schema schema, PathEntry pathEntry) {
        // If it's a terminal type.
        if (isTerminalType(fieldInfo.type())) {
            return fieldInfo.type();
        }

        Type fieldType = fieldInfo.type();

        if (fieldType.kind() == Type.Kind.WILDCARD_TYPE) {
            fieldType = resolveWildcard(fieldType.asWildcardType());
        }

        if (fieldType.kind() == Type.Kind.ARRAY) {
            LOG.debugv("Processing an array {0}", fieldInfo);
            ArrayType arrayType = fieldType.asArrayType();

            // TODO handle multi-dimensional arrays.

            // Array-type schema
            SchemaImpl arrSchema = new SchemaImpl();
            schema.type(Schema.SchemaType.ARRAY);
            schema.items(arrSchema);

            // Only use component (excludes the special name formatting for arrays).
            TypeUtil.TypeWithFormat typeFormat = TypeUtil.getTypeFormat(arrayType.component());
            arrSchema.setType(typeFormat.getSchemaType());
            arrSchema.setFormat(typeFormat.getFormat().format());

            // If it's not a terminal type, then push for later inspection.
            if (!isTerminalType(arrayType.component()) && indexContains(fieldType)) {
                ClassInfo klazz = getClassByName(fieldType);
                pushPathPair(pathEntry, klazz, arrSchema);
            }
            return arrayType;
        }

        if (isA(fieldType, ENUM_TYPE) && indexContains(fieldType)) {
            LOG.debugv("Processing an enum {0}", fieldInfo);
            ClassInfo enumKlazz = getClassByName(fieldType);

            for (FieldInfo enumField : enumKlazz.fields()) {
                // Ignore the hidden enum array as it's not accessible. Add fields that look like enums (of type enumKlazz)
                if (!enumField.name().equals("$VALUES") && TypeUtil.getName(enumField.type()).equals(enumKlazz.name())) {
                    // Enum's value fields.
                    schema.addEnumeration(enumField.name());
                }
            }
            return STRING_TYPE;
        }

        if (fieldType.kind() == Type.Kind.PARAMETERIZED_TYPE) {
            // Parameterised type (e.g. Foo<A, B>)
            return readParamType(pathEntry, schema, fieldType.asParameterizedType());
        }

        if (fieldType.kind() == Type.Kind.TYPE_VARIABLE) {
            // Type variable (e.g. A in Foo<A>)
            Type resolvedType = pathEntry.resolvedTypes.get(fieldType.asTypeVariable());

            LOG.debugv("Resolved type {0} -> {1}", fieldInfo, resolvedType);
            if (isTerminalType(resolvedType) || getClassByName(resolvedType) == null) {
                LOG.tracev("Is a terminal type {0}", resolvedType);
                TypeUtil.TypeWithFormat replacement = TypeUtil.getTypeFormat(resolvedType);
                schema.setType(replacement.getSchemaType());
                schema.setFormat(replacement.getFormat().format());
            } else {
                LOG.debugv("Attempting to do TYPE_VARIABLE substitution: {0} -> {1}", fieldInfo, resolvedType);

                // Look up the resolved type.
                ClassInfo klazz = getClassByName(resolvedType);

                // Resolving a type can result in another parameterised type,
                if (resolvedType.kind() == Type.Kind.PARAMETERIZED_TYPE) {
                    // The results must be made available to the node beneath this one.
                    // For example: Foo<Bar<String, Double>, Integer>
                    Map<TypeVariable, Type> resolutionMap = buildParamTypeResolutionMap(klazz, resolvedType.asParameterizedType());
                    PathEntry pair = new PathEntry(pathEntry, klazz, schema, resolutionMap);
                    path.push(pair);
                } else {
                    PathEntry entry = PathEntry.leafNode(pathEntry, klazz, schema);
                    path.push(entry);
                }
            }
            return resolvedType;
        }

        // Raw Collection
        if (isA(fieldType, COLLECTION_TYPE)) {
            return ARRAY_TYPE_OBJECT;
        }

        // Raw Map
        if (isA(fieldType, MAP_TYPE)) {
            return OBJECT_TYPE;
        }

        // Simple case: bare class or primitive type.
        if (indexContains(fieldType)) {
            pushFieldToPath(pathEntry, fieldInfo, schema);
        } else {
            // If the type is not in Jandex then we don't have easy access to it.
            // Future work could consider separate code to traverse classes reachable from this classloader.
            LOG.debugv("Encountered type not in Jandex index that is not well-known type. " +
                    "Will not traverse it: {0}", fieldType);
        }

        return fieldType;
    }

    private void readUnannotatedField(FieldInfo fieldInfo,
                                      Schema schema,
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

    private Type readParamType(PathEntry pathEntry, Schema schema, ParameterizedType pType) {
        LOG.debugv("Processing parameterized type {0}", pType);

        // If it's a collection, we should treat it as an array.
        if (isA(pType, COLLECTION_TYPE)) { // TODO maybe also Iterable?
            LOG.debugv("Processing Java Collection. Will treat as an array.");
            SchemaImpl arraySchema = new SchemaImpl();
            schema.type(Schema.SchemaType.ARRAY);
            schema.items(arraySchema);

            // E.g. In Foo<A, B> this will be: A, B
            for (Type argument : pType.arguments()) {
                if (isTerminalType(argument)) {
                    TypeUtil.TypeWithFormat terminalType = TypeUtil.getTypeFormat(argument);
                    arraySchema.type(terminalType.getSchemaType());
                    arraySchema.format(terminalType.getFormat().format());
                } else if (indexContains(argument)) {
                    ClassInfo klazz = getClassByName(argument);
                    pushPathPair(pathEntry, klazz, arraySchema);
                }
            }
            return ARRAY_TYPE_OBJECT; // Representing collection as JSON array
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
                } else if (indexContains(valueType)) {
                    ClassInfo klazz = getClassByName(valueType);
                    pushPathPair(pathEntry, klazz, propsSchema);
                }
                schema.additionalProperties(propsSchema);
            }
            return OBJECT_TYPE;
        } else {
            // This attempts to allow us to resolve the types generically.
            ClassInfo klazz = getClassByName(pType);
            // Build mapping of class's type variables (e.g. A, B) to resolved variables
            // Resolved variables could be any type (e.g. String, another param type, etc)
            Map<TypeVariable, Type> resolutionMap = buildParamTypeResolutionMap(klazz, pType);
            PathEntry pair = new PathEntry(pathEntry, klazz, schema, resolutionMap);
            path.push(pair);
            return pType;
        }
    }

    private Map<TypeVariable, Type> buildParamTypeResolutionMap(ClassInfo klazz, ParameterizedType parameterizedType) {
        List<Type> arguments = parameterizedType.arguments();
        List<TypeVariable> typeVariables = klazz.typeParameters();

        if (arguments.size() != typeVariables.size()) {
            LOG.errorv("Unanticipated mismatch between type arguments and type variables \n" +
                    "Args: {0}\n Vars:{1}", arguments, typeVariables);
        }

        Map<TypeVariable, Type> resolutionMap = new LinkedHashMap<>();
        for (int i = 0; i < arguments.size(); i++) {
            Type arg = arguments.get(i);
            TypeVariable typeVar = typeVariables.get(i);
            resolutionMap.put(typeVar, arg);
        }
        return resolutionMap;
    }

    // TODO: Super vs Extends behaviour.
    private Type resolveWildcard(WildcardType wildcardType) {
        return TypeUtil.getBound(wildcardType);
    }

    private void pushFieldToPath(PathEntry parentPathEntry,
                                 FieldInfo fieldInfo,
                                 Schema schema) {
        ClassType klazzType = fieldInfo.type().asClassType();
        ClassInfo klazzInfo = getClassByName(klazzType);
        pushPathPair(parentPathEntry, klazzInfo, schema);
    }

    private void pushPathPair(@NotNull PathEntry parentPathEntry,
                              @NotNull ClassInfo klazzInfo,
                              @NotNull Schema schema) {
        PathEntry entry = PathEntry.leafNode(parentPathEntry, klazzInfo, schema);
        if (parentPathEntry.hasParent(entry)) {
            // Cycle detected, don't push path.
            LOG.debugv("Possible cycle was detected at: {0}. Will not search further.", klazzInfo);
            LOG.tracev("Path: {0}", entry.toStringWithGraph());
            if (schema.getDescription() == null) {
                schema.description("Cyclic reference to " + klazzInfo.name());
            }
        } else {
            // Push path to be inspected later.
            LOG.debugv("Adding child node to path: {0}", klazzInfo);
            path.push(entry);
        }
    }

    private ClassInfo getClassByName(@NotNull Type type) {
        return index.getClassByName(TypeUtil.getName(type));
    }

    private boolean indexContains(@NotNull Type type) {
        return getClassByName(type) != null;
    }

    private boolean isA(Type testSubject, Type test) {
        return TypeUtil.isA(index, testSubject, test);
    }

    private boolean isTerminalType(Type type) {
        if (type.kind() == Type.Kind.TYPE_VARIABLE ||
                type.kind() == Type.Kind.WILDCARD_TYPE ||
                type.kind() == Type.Kind.ARRAY) {
            return false;
        }

        if (type.kind() == Type.Kind.PRIMITIVE ||
                type.kind() == Type.Kind.VOID) {
            return true;
        }

        TypeUtil.TypeWithFormat tf = TypeUtil.getTypeFormat(type);
        // If is known type.
        return tf.getSchemaType() != Schema.SchemaType.OBJECT &&
                tf.getSchemaType() != Schema.SchemaType.ARRAY;
    }

    private boolean shouldInferUnannotatedFields() {
        String infer = System.getProperties().getProperty("openapi.infer-unannotated-types", "true");
        return Boolean.parseBoolean(infer);
    }

    // Needed for non-recursive DFS to keep schema and class together.
    private static final class PathEntry {
        private final PathEntry parent;
        private final ClassInfo clazz;
        private final Schema schema;
        private final Map<TypeVariable, Type> resolvedTypes;

        PathEntry(PathEntry parent,
                  @NotNull ClassInfo clazz,
                  @NotNull Schema schema) {
            this.parent = parent;
            this.clazz = clazz;
            this.schema = schema;
            this.resolvedTypes = Collections.emptyMap();
        }

        PathEntry(PathEntry parent,
                  @NotNull ClassInfo clazz,
                  @NotNull Schema schema,
                  @NotNull Map<TypeVariable, Type> types) {
            this.parent = parent;
            this.clazz = clazz;
            this.schema = schema;
            this.resolvedTypes = types;
        }

        static PathEntry rootNode(ClassInfo classInfo, Schema rootSchema) {
            return new PathEntry(null, classInfo, rootSchema);
        }

        static PathEntry leafNode(PathEntry parentNode, ClassInfo classInfo, Schema rootSchema) {
            return new PathEntry(parentNode, classInfo, rootSchema);
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
    }

}
