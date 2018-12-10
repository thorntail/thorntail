/*
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
package org.wildfly.swarm.microprofile.faulttolerance.deployment;

import io.smallrye.faulttolerance.DefaultHystrixConcurrencyStrategy;

import javax.annotation.Resource;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.enterprise.util.AnnotationLiteral;

/**
 *
 * @author Martin Kouba
 */
public class MicroProfileFaultToleranceExtension implements Extension {

    void beforeBeanDiscovery(@Observes BeforeBeanDiscovery event, BeanManager beanManager) {
        event.addAnnotatedType(beanManager.createAnnotatedType(RequestContextCommandListener.class), RequestContextCommandListener.class.getName());
        event.addAnnotatedType(beanManager.createAnnotatedType(WeldCommandListenersProvider.class), WeldCommandListenersProvider.class.getName());
    }

    // workaround for WFLY-11373, see also THORN-2271
    void processAnnotatedType(@Observes ProcessAnnotatedType<DefaultHystrixConcurrencyStrategy> event) {
        event.configureAnnotatedType()
                .filterFields(field -> "managedThreadFactory".equals(field.getJavaMember().getName()))
                .forEach(field -> {
                    field.remove(annotation -> Resource.class.equals(annotation.annotationType()));
                    field.add(ResourceLiteral.lookup("java:jboss/ee/concurrency/factory/default"));
                });
    }

    static class ResourceLiteral extends AnnotationLiteral<Resource> implements Resource {
        private static final long serialVersionUID = 1L;

        private final String name;
        private final String lookup;
        private final Class<?> type;
        private final AuthenticationType authenticationType;
        private final boolean shareable;
        private final String mappedName;
        private final String description;

        static ResourceLiteral lookup(String lookup) {
            return new ResourceLiteral(null, lookup, null, null, null, null, null);
        }

        private ResourceLiteral(String name, String lookup, Class<?> type, AuthenticationType authenticationType,
                                Boolean shareable, String mappedName, String description) {
            this.name = name == null ? "" : name;
            this.lookup = lookup == null ? "" : lookup;
            this.type = type == null ? Object.class : type;
            this.authenticationType = authenticationType == null ? AuthenticationType.CONTAINER : authenticationType;
            this.shareable = shareable == null ? true : shareable.booleanValue();
            this.mappedName = mappedName == null ? "" : mappedName;
            this.description = description == null ? "" : description;
        }

        @Override
        public String name() {
            return name;
        }

        @Override
        public String lookup() {
            return lookup;
        }

        @Override
        public Class<?> type() {
            return type;
        }

        @Override
        public AuthenticationType authenticationType() {
            return authenticationType;
        }

        @Override
        public boolean shareable() {
            return shareable;
        }

        @Override
        public String mappedName() {
            return mappedName;
        }

        @Override
        public String description() {
            return description;
        }
    }
}
