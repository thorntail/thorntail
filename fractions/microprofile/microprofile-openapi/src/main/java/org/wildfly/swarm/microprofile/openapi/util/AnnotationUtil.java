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
import java.util.Arrays;
import java.util.List;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationValue;
import org.jboss.jandex.AnnotationValue.Kind;

/**
 * Class that provides some generally useful methods for reading annotations.
 * @author eric.wittmann@gmail.com
 */
public class AnnotationUtil {

    /**
     * Constructor.
     */
    private AnnotationUtil() {
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
        if (value.kind() == Kind.DOUBLE) {
            return new BigDecimal(value.asDouble());
        }
        if (value.kind() == Kind.STRING) {
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

}
