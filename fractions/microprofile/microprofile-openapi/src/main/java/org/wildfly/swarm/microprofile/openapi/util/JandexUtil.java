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

package org.wildfly.swarm.microprofile.openapi.util;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationTarget;
import org.jboss.jandex.AnnotationTarget.Kind;
import org.jboss.jandex.AnnotationValue;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.IndexView;
import org.jboss.jandex.MethodInfo;
import org.wildfly.swarm.microprofile.openapi.OpenApiConstants;

import com.fasterxml.jackson.jaxrs.cfg.Annotations;

/**
 * Some utility methods for working with Jandex objects.
 * @author eric.wittmann@gmail.com
 */
public class JandexUtil {

    /**
     * Constructor.
     */
    private JandexUtil() {
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
            return Arrays.asList(value.asStringArray());
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
        Collection<AnnotationInstance> annotations = ct.classAnnotations();
        for (AnnotationInstance annotationInstance : annotations) {
            if (annotationInstance.name().equals(name)) {
                return annotationInstance;
            }
        }
        return null;
    }

    /**
     * Returns a list of annotations of a given type found on the given method.
     * @param method
     * @param annotationName
     */
    public static List<AnnotationInstance> getAnnotations(MethodInfo method, DotName annotationName) {
        List<AnnotationInstance> annotations = new ArrayList<>(method.annotations());
        CollectionUtils.filter(annotations, new Predicate() {
            @Override
            public boolean evaluate(Object object) {
                AnnotationInstance annotation = (AnnotationInstance) object;
                return annotation.name().equals(annotationName);
            }
        });
        return annotations;
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
        String[] split = ref.split("/");
        return split[split.length - 1];
    }

}
