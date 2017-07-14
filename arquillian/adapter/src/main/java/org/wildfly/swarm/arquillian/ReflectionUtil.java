/**
 * Copyright 2015-2016 Red Hat, Inc, and individual contributors.
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
package org.wildfly.swarm.arquillian;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.List;

/**
 * @author alexsoto
 */
public final class ReflectionUtil {

    /**
     * No instantiation
     */
    private ReflectionUtil() {
        throw new UnsupportedOperationException("No instantiation");
    }

    /**
     * Returns all methods annotated with given annotation.
     * @param source class with methods.
     * @param annotationClass that is found.
     * @return List of methods annotated with annotationClass.
     */
    public static List<Method> getMethodsWithAnnotation(final Class<?> source,
                                                        final Class<? extends Annotation> annotationClass) {
        List<Method> declaredAccessableMethods = AccessController
                .doPrivileged((PrivilegedAction<List<Method>>) () -> {
                    List<Method> foundMethods = new ArrayList<>();
                    for (Method method : source.getMethods()) {
                        if (method.isAnnotationPresent(annotationClass)) {
                            if (!method.isAccessible()) {
                                method.setAccessible(true);
                            }
                            foundMethods.add(method);
                        }
                    }
                    return foundMethods;
                });
        return declaredAccessableMethods;
    }
}
