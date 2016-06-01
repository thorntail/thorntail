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
import java.lang.reflect.Modifier;

import org.wildfly.swarm.ContainerFactory;
import org.wildfly.swarm.container.Container;

/**
 * @author Bob McWhirter
 */
public class AnnotationBasedContainerFactory implements ContainerFactory {
    public static final String ANNOTATED_CLASS_NAME = "swarm.arquillian.container.factory.class";

    @Override
    public Container newContainer(String... args) throws Exception {
        String clsName = System.getProperty(ANNOTATED_CLASS_NAME);

        Class<?> cls = Class.forName(clsName);

        Method[] methods = cls.getMethods();

        for (Method method : methods) {
            if (! Modifier.isStatic( method.getModifiers() ) ) {
                continue;
            }

            org.wildfly.swarm.arquillian.adapter.Container anno = method.getAnnotation(org.wildfly.swarm.arquillian.adapter.Container.class);
            if ( anno == null ) {
                continue;
            }

            return (Container) method.invoke( null );
        }

        throw new RuntimeException( "Unable to create a Container from annotated class: " + clsName );
    }
}
