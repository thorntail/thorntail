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

import org.jboss.shrinkwrap.api.Archive;
import org.wildfly.swarm.ContainerFactory;
import org.wildfly.swarm.arquillian.ReflectionUtil;
import org.wildfly.swarm.arquillian.daemon.DaemonServiceActivator;
import org.wildfly.swarm.bootstrap.util.BootstrapProperties;
import org.wildfly.swarm.container.Container;
import org.wildfly.swarm.msc.ServiceActivatorArchive;
import org.wildfly.swarm.spi.api.JARArchive;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;

/**
 * @author Toby Crawley
 * @author alexsoto
 */
public class InVMSimpleContainer implements SimpleContainer {
    public InVMSimpleContainer(Class<?> testClass) {
        this.testClass = testClass;
    }

    @Override
    public void start(Archive<?> archive) throws Exception {
        archive.as(ServiceActivatorArchive.class)
                .addServiceActivator(DaemonServiceActivator.class)
                .as(JARArchive.class)
                .addModule("org.wildfly.swarm.arquillian.daemon");

        System.setProperty(BootstrapProperties.APP_ARTIFACT, archive.getName());

        if (isContainerFactory(this.testClass)) {

            archive.as(JARArchive.class).addModule("org.wildfly.swarm.container");
            archive.as(JARArchive.class).addModule("org.wildfly.swarm.configuration");
            Object factory = this.testClass.newInstance();
            this.container = ((ContainerFactory) factory).newContainer();

        } else {

            Method containerMethod = getAnnotatedMethodWithContainer(this.testClass);

            if (containerMethod != null) {
                if (Modifier.isStatic(containerMethod.getModifiers())) {
                    final Object container = containerMethod.invoke(null, new Object[0]);

                    if (container instanceof Container) {
                        this.container = (Container) container;
                    } else {
                        throw new IllegalArgumentException(
                                String.format("Method annotated with %s does not return an instace of %s",
                                        org.wildfly.swarm.arquillian.adapter.Container.class.getSimpleName(),
                                        Container.class.getSimpleName()));
                    }
                } else {
                    throw new IllegalArgumentException(
                            String.format("Method annotated with %s is %s but it is not static",
                                    org.wildfly.swarm.arquillian.adapter.Container.class.getSimpleName(),
                                    containerMethod));
                }
            } else {
                this.container = new Container();
            }
        }
        this.container.start().deploy(archive);
    }

    /**
     * Returns the method that is annotated with @{@link org.wildfly.swarm.arquillian.adapter.Container}.
     * Throws an exception if more than one method is found annotated.
     * @param testClass where annotation is searched.
     * @return Method annotated with @{@link org.wildfly.swarm.arquillian.adapter.Container}
     * or null if no method annotated.
     */
    private Method getAnnotatedMethodWithContainer(Class<?> testClass) {
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

    @Override
    public void stop() throws Exception {
        if (container != null) {
            container.stop();
        }
    }

    private final Class<?> testClass;

    private Container container;
}
