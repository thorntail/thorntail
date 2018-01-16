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

import java.util.Arrays;
import java.util.List;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationValue;

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

}
