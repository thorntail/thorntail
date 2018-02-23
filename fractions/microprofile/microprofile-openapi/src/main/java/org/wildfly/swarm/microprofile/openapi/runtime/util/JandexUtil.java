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

package org.wildfly.swarm.microprofile.openapi.runtime.util;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.eclipse.microprofile.openapi.models.parameters.Parameter;
import org.eclipse.microprofile.openapi.models.parameters.Parameter.In;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationTarget;
import org.jboss.jandex.AnnotationTarget.Kind;
import org.wildfly.swarm.microprofile.openapi.runtime.OpenApiConstants;
import org.jboss.jandex.AnnotationValue;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.IndexView;
import org.jboss.jandex.MethodInfo;
import org.jboss.jandex.Type;

import com.fasterxml.jackson.jaxrs.cfg.Annotations;

/**
 * Some utility methods for working with Jandex objects.
 * @author eric.wittmann@gmail.com
 */
public class JandexUtil {

    /**
     * Simple enum to indicate the type of a $ref being read/written.
     * @author eric.wittmann@gmail.com
     */
    public enum RefType {
        Header, Schema, SecurityScheme, Callback, Link, Response, Parameter, Example, RequestBody
    }

    /**
     * Constructor.
     */
    private JandexUtil() {
    }

    /**
     * Reads a string property named "ref" value from the given annotation and converts it
     * to a value appropriate for setting on a model's "$ref" property.
     * @param annotation
     * @param refType
     */
    public static String refValue(AnnotationInstance annotation, RefType refType) {
        AnnotationValue value = annotation.value(OpenApiConstants.PROP_REF);
        if (value == null) {
            return null;
        }

        String $ref = value.asString();
        if ($ref.startsWith("#/")) {
            return $ref;
        }

        switch (refType) {
            case Callback:
                $ref = "#/components/callbacks/" + $ref;
                break;
            case Example:
                $ref = "#/components/examples/" + $ref;
                break;
            case Header:
                $ref = "#/components/headers/" + $ref;
                break;
            case Link:
                $ref = "#/components/links/" + $ref;
                break;
            case Parameter:
                $ref = "#/components/parameters/" + $ref;
                break;
            case RequestBody:
                $ref = "#/components/requestBodies/" + $ref;
                break;
            case Response:
                $ref = "#/components/responses/" + $ref;
                break;
            case Schema:
                $ref = "#/components/schemas/" + $ref;
                break;
            case SecurityScheme:
                $ref = "#/components/securitySchemes/" + $ref;
                break;
            default:
                throw new IllegalStateException("Unexpected Jandex RefType " + refType);
        }
        return $ref;
    }

    /**
     * Reads a String property value from the given annotation instance.  If no value is found
     * this will return null.
     * @param annotation
     * @param propertyName
     */
    public static String stringValue(AnnotationInstance annotation, String propertyName) {
        AnnotationValue value = annotation.value(propertyName);
        if (value == null) {
            return null;
        } else {
            return value.asString();
        }
    }

    /**
     * Reads a Boolean property value from the given annotation instance.  If no value is found
     * this will return null.
     * @param annotation
     * @param propertyName
     */
    public static Boolean booleanValue(AnnotationInstance annotation, String propertyName) {
        AnnotationValue value = annotation.value(propertyName);
        if (value == null) {
            return null;
        } else {
            return value.asBoolean();
        }
    }

    public static Boolean booleanValueWithDefault(AnnotationInstance annotation, String propertyName) {
        AnnotationValue value = annotation.value(propertyName);
        return value != null && value.asBoolean();
    }

    /**
     * Reads a Double property value from the given annotation instance.  If no value is found
     * this will return null.
     * @param annotation
     * @param propertyName
     */
    public static BigDecimal bigDecimalValue(AnnotationInstance annotation, String propertyName) {
        AnnotationValue value = annotation.value(propertyName);
        if (value == null) {
            return null;
        }
        if (value.kind() == AnnotationValue.Kind.DOUBLE) {
            return new BigDecimal(value.asDouble());
        }
        if (value.kind() == AnnotationValue.Kind.STRING) {
            return new BigDecimal(value.asString());
        }
        throw new RuntimeException("Call to bigDecimalValue failed because the annotation property was not a double or a String.");
    }

    /**
     * Reads a Integer property value from the given annotation instance.  If no value is found
     * this will return null.
     * @param annotation
     * @param propertyName
     */
    public static Integer intValue(AnnotationInstance annotation, String propertyName) {
        AnnotationValue value = annotation.value(propertyName);
        if (value == null) {
            return null;
        } else {
            return value.asInt();
        }
    }

    /**
     * Reads a String array property value from the given annotation instance.  If no value is found
     * this will return null.
     * @param annotation
     * @param propertyName
     * @return
     */
    public static List<String> stringListValue(AnnotationInstance annotation, String propertyName) {
        AnnotationValue value = annotation.value(propertyName);
        if (value == null) {
            return null;
        } else {
            return new ArrayList<>(Arrays.asList(value.asStringArray()));
        }
    }

    /**
     * Reads a String property value from the given annotation instance.  If no value is found
     * this will return null.
     * @param annotation
     * @param propertyName
     */
    @SuppressWarnings("rawtypes")
    public static <T extends Enum> T enumValue(AnnotationInstance annotation, String propertyName, Class<T> clazz) {
        AnnotationValue value = annotation.value(propertyName);
        if (value == null) {
            return null;
        }
        String strVal = value.asString();
        T[] constants = clazz.getEnumConstants();
        for (T t : constants) {
            if (t.name().equals(strVal)) {
                return t;
            }
        }
        for (T t : constants) {
            if (t.name().equalsIgnoreCase(strVal)) {
                return t;
            }
        }
        return null;
    }

    /**
     * Returns true if the given annotation instance is a "ref".  An annotation is a ref if it has
     * a non-null value for the "ref" property.
     * @param annotation
     */
    public static boolean isRef(AnnotationInstance annotation) {
        return annotation.value(OpenApiConstants.PROP_REF) != null;
    }

    /**
     * Returns true if the given annotation is void of any values (and thus is "empty").  An example
     * of this would be if a jax-rs method were annotated with @Tag()
     * @param annotation
     */
    public static boolean isEmpty(AnnotationInstance annotation) {
        return Annotations.values().length == 0;
    }

    /**
     * Gets a single class annotation from the given class.  Returns null if no matching annotation
     * is found.
     * @param ct
     * @param name
     */
    public static AnnotationInstance getClassAnnotation(ClassInfo ct, DotName name) {
        if (name == null) {
            return null;
        }
        Collection<AnnotationInstance> annotations = ct.classAnnotations();
        for (AnnotationInstance annotationInstance : annotations) {
            if (annotationInstance.name().equals(name)) {
                return annotationInstance;
            }
        }
        return null;
    }

    /**
     * Use the jandex index to find all jax-rs resource classes.  This is done by searching for
     * all Class-level @Path annotations.
     * @param pathAnnotations
     * @param index
     */
    public static Collection<ClassInfo> getJaxRsResourceClasses(IndexView index) {
        Collection<ClassInfo> resourceClasses = new ArrayList<>();
        Collection<AnnotationInstance> pathAnnotations = index.getAnnotations(OpenApiConstants.DOTNAME_PATH);
        for (AnnotationInstance pathAnno : pathAnnotations) {
            AnnotationTarget annotationTarget = pathAnno.target();
            if (annotationTarget.kind() == AnnotationTarget.Kind.CLASS) {
                resourceClasses.add(annotationTarget.asClass());
            }
        }
        return resourceClasses;
    }

    /**
     * Returns all annotations configured for a single parameter of a method.
     * @param method
     * @param paramPosition
     */
    public static List<AnnotationInstance> getParameterAnnotations(MethodInfo method, short paramPosition) {
        List<AnnotationInstance> annotations = new ArrayList<>(method.annotations());
        CollectionUtils.filter(annotations, new Predicate() {
            @Override
            public boolean evaluate(Object object) {
                AnnotationInstance annotation = (AnnotationInstance) object;
                AnnotationTarget target = annotation.target();
                return target != null && target.kind() == Kind.METHOD_PARAMETER && target.asMethodParameter().position() == paramPosition;
            }
        });
        return annotations;
    }

    /**
     * Gets the name of an item from its ref.  For example, the ref might be "#/components/parameters/departureDate"
     * which would result in a name of "departureDate".
     * @param annotation
     */
    public static String nameFromRef(AnnotationInstance annotation) {
        String ref = annotation.value(OpenApiConstants.PROP_REF).asString();
        return ModelUtil.nameFromRef(ref);
    }

    /**
     * Many OAI annotations can either be found singly or as a wrapped array.  This method will
     * look for both and return a list of all found.  Both the single and wrapper annotation names
     * must be provided.
     * @param method
     * @param singleAnnotationName
     * @param repeatableAnnotationName
     */
    public static List<AnnotationInstance> getRepeatableAnnotation(MethodInfo method,
            DotName singleAnnotationName, DotName repeatableAnnotationName) {
        List<AnnotationInstance> annotations = new ArrayList<>(method.annotations());
        CollectionUtils.filter(annotations, new Predicate() {
            @Override
            public boolean evaluate(Object object) {
                AnnotationInstance annotation = (AnnotationInstance) object;
                return annotation.name().equals(singleAnnotationName);
            }
        });
        if (repeatableAnnotationName != null && method.hasAnnotation(repeatableAnnotationName)) {
            AnnotationInstance annotation = method.annotation(repeatableAnnotationName);
            AnnotationValue annotationValue = annotation.value();
            if (annotationValue != null) {
                AnnotationInstance[] nestedArray = annotationValue.asNestedArray();
                annotations.addAll(Arrays.asList(nestedArray));
            }
        }
        return annotations;
    }

    /**
     * Many OAI annotations can either be found singly or as a wrapped array.  This method will
     * look for both and return a list of all found.  Both the single and wrapper annotation names
     * must be provided.
     * @param clazz
     * @param singleAnnotationName
     * @param repeatableAnnotationName
     */
    public static List<AnnotationInstance> getRepeatableAnnotation(ClassInfo clazz,
            DotName singleAnnotationName, DotName repeatableAnnotationName) {
        List<AnnotationInstance> annotations = new ArrayList<>();
        AnnotationInstance single = JandexUtil.getClassAnnotation(clazz, singleAnnotationName);
        AnnotationInstance repeatable = JandexUtil.getClassAnnotation(clazz, repeatableAnnotationName);
        if (single != null) {
            annotations.add(single);
        }
        if (repeatable != null) {
            AnnotationValue annotationValue = repeatable.value();
            if (annotationValue != null) {
                AnnotationInstance[] nestedArray = annotationValue.asNestedArray();
                annotations.addAll(Arrays.asList(nestedArray));
            }
        }
        return annotations;
    }

    /**
     * Returns the class type of the method parameter at the given position.
     * @param method
     * @param position
     */
    public static Type getMethodParameterType(MethodInfo method, short position) {
        Type type = method.parameters().get(position);
        return type;
    }

    /**
     * Go through the method parameters looking for one that is not annotated with a jax-rs
     * annotation.  That will be the one that is the request body.
     * @param method
     */
    public static Type getRequestBodyParameterClassType(MethodInfo method) {
        List<Type> methodParams = method.parameters();
        if (methodParams.isEmpty()) {
            return null;
        }
        for (short i = 0; i < methodParams.size(); i++) {
            if (JandexUtil.getParameterAnnotations(method, i).isEmpty()) {
                return methodParams.get(i).asClassType();
            }
        }
        return null;
    }

    /**
     * Returns jax-rs info about the parameter at the given index.  If the index is invalid
     * or does not refer to a jax-rs parameter (ie is not annoted with e.g. @PathParam) then
     * this will return null.  Otherwise it will return a {@link JaxRsParameterInfo} object
     * with the name and type of the param.
     * @param method
     * @param idx
     */
    public static JaxRsParameterInfo getMethodParameterJaxRsInfo(MethodInfo method, int idx) {
        AnnotationInstance jaxRsAnno = JandexUtil.getMethodParameterAnnotation(method, idx, OpenApiConstants.DOTNAME_PATH_PARAM);
        if (jaxRsAnno != null) {
            JaxRsParameterInfo info = new JaxRsParameterInfo();
            info.in = In.PATH;
            info.name = JandexUtil.stringValue(jaxRsAnno, OpenApiConstants.PROP_VALUE);
            return info;
        }

        jaxRsAnno = JandexUtil.getMethodParameterAnnotation(method, idx, OpenApiConstants.DOTNAME_QUERY_PARAM);
        if (jaxRsAnno != null) {
            JaxRsParameterInfo info = new JaxRsParameterInfo();
            info.in = In.QUERY;
            info.name = JandexUtil.stringValue(jaxRsAnno, OpenApiConstants.PROP_VALUE);
            return info;
        }

        jaxRsAnno = JandexUtil.getMethodParameterAnnotation(method, idx, OpenApiConstants.DOTNAME_COOKIE_PARAM);
        if (jaxRsAnno != null) {
            JaxRsParameterInfo info = new JaxRsParameterInfo();
            info.in = In.COOKIE;
            info.name = JandexUtil.stringValue(jaxRsAnno, OpenApiConstants.PROP_VALUE);
            return info;
        }

        jaxRsAnno = JandexUtil.getMethodParameterAnnotation(method, idx, OpenApiConstants.DOTNAME_HEADER_PARAM);
        if (jaxRsAnno != null) {
            JaxRsParameterInfo info = new JaxRsParameterInfo();
            info.in = In.HEADER;
            info.name = JandexUtil.stringValue(jaxRsAnno, OpenApiConstants.PROP_VALUE);
            return info;
        }

        return null;
    }

    /**
     * Finds an annotation (if present) with the given name, on a particular parameter of a
     * method.  Returns null if not found.
     * @param method
     * @param parameterIndex
     * @param annotationName
     */
    private static AnnotationInstance getMethodParameterAnnotation(MethodInfo method, int parameterIndex,
            DotName annotationName) {
        for (AnnotationInstance annotation : method.annotations()) {
            if (annotation.target().kind() == Kind.METHOD_PARAMETER &&
                    annotation.target().asMethodParameter().position() == parameterIndex &&
                    annotation.name().equals(annotationName)) {
                return annotation;
            }
        }
        return null;
    }

    /**
     * Returns true if the given @Schema annotation is a simple class schema.  This means that
     * the annotation only has one field defined, and that field is "implementation".
     * @param annotation
     */
    public static boolean isSimpleClassSchema(AnnotationInstance annotation) {
        List<AnnotationValue> values = annotation.values();
        return values.size() == 1 && values.get(0).name().equals(OpenApiConstants.PROP_IMPLEMENTATION);
    }

    /**
     * Returns true if the given @Schema annotation is a simple array schema.  This is defined
     * as a schema with only a "type" field and "implementation" field defined *and* the type must
     * be array.
     * @param annotation
     */
    public static boolean isSimpleArraySchema(AnnotationInstance annotation) {
        List<AnnotationValue> values = annotation.values();
        if (values.size() != 2) {
            return false;
        }
        org.eclipse.microprofile.openapi.models.media.Schema.SchemaType type =
                JandexUtil.enumValue(annotation, OpenApiConstants.PROP_TYPE, org.eclipse.microprofile.openapi.models.media.Schema.SchemaType.class);
        String implementation = JandexUtil.stringValue(annotation, OpenApiConstants.PROP_IMPLEMENTATION);
        return (type == org.eclipse.microprofile.openapi.models.media.Schema.SchemaType.ARRAY && implementation != null);
    }

    /**
     * Holds relevant information about a jax-rs method parameter.  Specifically its name
     * and type (path, query, cookie, etc).
     * @author eric.wittmann@gmail.com
     */
    public static class JaxRsParameterInfo {
        public String name;
        public Parameter.In in;
    }

}
