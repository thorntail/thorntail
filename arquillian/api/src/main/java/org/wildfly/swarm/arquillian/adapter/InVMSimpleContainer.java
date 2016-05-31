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

            Method containerMethod = getAnnotatedMethodWithAnnotation(this.testClass,
                    org.wildfly.swarm.arquillian.adapter.Container.class);
            // If there is a method annotated with @Container
            if (containerMethod != null) {
                if (Modifier.isStatic(containerMethod.getModifiers())) {
                    final Object container = containerMethod.invoke(null, new Object[0]);

                    if (container instanceof Container) {
                        this.container = (Container) container;
                    } else {
                        throw new IllegalArgumentException(
                                String.format("Method annotated with %s does not return an instance of %s",
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

                Method containerFactoryMethod = getAnnotatedMethodWithAnnotation(this.testClass,
                        org.wildfly.swarm.arquillian.adapter.ContainerFactory.class);

                // If there is a method annotated with @ContainerFactory
                if (containerFactoryMethod != null) {
                    if (Modifier.isStatic(containerFactoryMethod.getModifiers())) {
                        final Object containerFactory = containerFactoryMethod.invoke(null, new Object[0]);

                        if (containerFactory instanceof Class) {
                            Class containerFactoryClass = (Class) containerFactory;
                            if (ContainerFactory.class.isAssignableFrom(containerFactoryClass)) {
                                Object factory = containerFactoryClass.newInstance();
                                this.container = ((ContainerFactory) factory).newContainer();
                            } else {
                                throw new IllegalArgumentException(
                                        String.format("Method annotated with %s does not return a class of %s",
                                                org.wildfly.swarm.arquillian.adapter.ContainerFactory.class.getSimpleName(),
                                                ContainerFactory.class.getSimpleName()));
                            }

                        } else {
                            throw new IllegalArgumentException(
                                    String.format("Method annotated with %s does not return a class of %s",
                                            org.wildfly.swarm.arquillian.adapter.ContainerFactory.class.getSimpleName(),
                                            ContainerFactory.class.getSimpleName()));
                        }
                    } else {
                        throw new IllegalArgumentException(
                                String.format("Method annotated with %s is %s but it is not static",
                                        org.wildfly.swarm.arquillian.adapter.ContainerFactory.class.getSimpleName(),
                                        containerMethod));
                    }
                } else {
                    this.container = new Container();
                }
            }
        }
        this.container.start().deploy(archive);
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
