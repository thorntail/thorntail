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

import org.wildfly.swarm.Swarm;
import org.wildfly.swarm.arquillian.CreateSwarm;

/**
 * A main class capable of using another nother class, annotated with {@link CreateSwarm} to create a main().
 *
 * @uathor Ken Finnigan
 */
public class AnnotationBasedMain {
    public static final String ANNOTATED_CLASS_NAME = "thorntail.arquillian.createswarm.class";

    protected AnnotationBasedMain() {
    }

    public static void main(String... args) throws Exception {
        if (System.getProperty("boot.module.loader") == null) {
            System.setProperty("boot.module.loader", "org.wildfly.swarm.bootstrap.modules.BootModuleLoader");
        }

        String clsName = System.getProperty(ANNOTATED_CLASS_NAME);

        Class<?> cls = Class.forName(clsName);

        Method[] methods = cls.getMethods();

        for (Method method : methods) {
            if (!Modifier.isStatic(method.getModifiers())) {
                continue;
            }

            CreateSwarm anno = method.getAnnotation(CreateSwarm.class);
            if (anno == null) {
                continue;
            }

            boolean startEagerly = anno.startEagerly();
            ((Swarm) method.invoke(null)).start().deploy();
        }
    }

}
