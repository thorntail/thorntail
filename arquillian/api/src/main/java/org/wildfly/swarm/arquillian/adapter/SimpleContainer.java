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

import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;

import org.jboss.shrinkwrap.api.Archive;
import org.wildfly.swarm.arquillian.ReflectionUtil;

public interface SimpleContainer {
    void start(Archive<?> archive) throws Exception;

    void stop() throws Exception;

    default SimpleContainer requestedMavenArtifacts(Set<String> artifacts) {
        return this;
    }

    default boolean isContainerFactory(Class<?> cls) {
        if (cls.getName().equals("org.wildfly.swarm.ContainerFactory")) {
            return true;
        }

        for (Class<?> interf : cls.getInterfaces()) {
            if (isContainerFactory(interf)) {
                return true;
            }
        }

        return cls.getSuperclass() != null && isContainerFactory(cls.getSuperclass());
    }

    /**
     * Returns the method that is annotated with @{@link org.wildfly.swarm.arquillian.adapter.Container}.
     * Throws an exception if more than one method is found annotated.
     * @param testClass where annotation is searched.
     * @return Method annotated with @{@link org.wildfly.swarm.arquillian.adapter.Container}
     * or null if no method annotated.
     */
    default Method getAnnotatedMethodWithContainer(Class<?> testClass) {
        final List<Method> methodsWithContainerAnnotation = ReflectionUtil.getMethodsWithAnnotation(testClass,
                org.wildfly.swarm.arquillian.adapter.Container.class);

        if (methodsWithContainerAnnotation.size() > 1 ) {
            throw new IllegalArgumentException(
                    String.format("More than one %s annotation found and only one was expected. Methods where %s was found are; %s",
                            org.wildfly.swarm.arquillian.adapter.Container.class.getSimpleName(),
                            org.wildfly.swarm.arquillian.adapter.Container.class.getSimpleName(),
                            methodsWithContainerAnnotation));
        }

        return methodsWithContainerAnnotation.size() == 1 ? methodsWithContainerAnnotation.get(0) : null;
    }

}
