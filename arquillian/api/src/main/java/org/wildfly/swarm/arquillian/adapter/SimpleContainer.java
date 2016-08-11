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
package org.wildfly.swarm.arquillian.adapter;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;

import org.jboss.shrinkwrap.api.Archive;
import org.wildfly.swarm.arquillian.ReflectionUtil;

public interface SimpleContainer {
    void start(Archive<?> archive) throws Exception;

    void stop() throws Exception;

    SimpleContainer setJavaVmArguments(String javaVmArguments);

    default SimpleContainer requestedMavenArtifacts(Set<String> artifacts) {
        return this;
    }

    /**
     * Returns the method that is annotated with given annotation.
     * Throws an exception if more than one method is found annotated.
     *
     * @param testClass  where annotation is searched.
     * @param annotation type of annotation
     * @return Method annotated with given annotation
     * or null if no method annotated.
     */
    default Method getAnnotatedMethodWithAnnotation(Class<?> testClass, Class<? extends Annotation> annotation) {
        final List<Method> methodsWithAnnotation = ReflectionUtil.getMethodsWithAnnotation(testClass,
                                                                                           annotation);

        if (methodsWithAnnotation.size() > 1) {
            throw new IllegalArgumentException(
                    String.format("More than one %s annotation found and only one was expected. Methods where %s was found are; %s",
                                  annotation.getSimpleName(),
                                  annotation.getSimpleName(),
                                  methodsWithAnnotation));
        }

        return methodsWithAnnotation.size() == 1 ? methodsWithAnnotation.get(0) : null;
    }

}
