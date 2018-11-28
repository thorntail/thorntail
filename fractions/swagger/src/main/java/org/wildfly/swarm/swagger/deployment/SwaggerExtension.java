/*
 * Copyright 2015-2018 Red Hat, Inc, and individual contributors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wildfly.swarm.swagger.deployment;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterTypeDiscovery;
import javax.enterprise.inject.spi.AnnotatedConstructor;
import javax.enterprise.inject.spi.AnnotatedField;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.ws.rs.core.Application;

/**
 * CDI extension for wrapping around types that extend the JAX-RS application.
 */
public class SwaggerExtension implements Extension {

    private static final Annotation ADD_SWAGGER_ANNOTATION = () -> AddSwaggerResources.class;

    /**
     * Associate the InterceptorBinding annotation.
     */
    public void processBeforeBeanDiscovery(@Observes BeforeBeanDiscovery event, BeanManager beanManager) {
        event.addInterceptorBinding(beanManager.createAnnotatedType(AddSwaggerResources.class));
        event.addAnnotatedType(beanManager.createAnnotatedType(SwaggerRestApplicationInterceptor.class), SwaggerRestApplicationInterceptor.class.getName());
    }

    /**
     * Associate the interceptor.
     */
    public void processAfterTypeDiscovery(@Observes AfterTypeDiscovery event) {
        if (!event.getInterceptors().contains(SwaggerRestApplicationInterceptor.class)) {
            event.getInterceptors().add(SwaggerRestApplicationInterceptor.class);
        }
    }

    /**
     * Associate the {@link AddSwaggerResources} interceptor binding with any bean that is a subclass of {@link Application}.
     */
    public <T extends Application> void processAnnotatedType(@Observes ProcessAnnotatedType<T> event) {

        AnnotatedType<T> annotatedType = event.getAnnotatedType();
        if (Application.class.isAssignableFrom(annotatedType.getJavaClass())) {
            // Do our magic.
            event.setAnnotatedType(new AnnotatedType<T>() {
                private final Annotation ADD_SWAGGER_ANNOTATION = () -> AddSwaggerResources.class;

                @Override
                public Class<T> getJavaClass() {
                    return annotatedType.getJavaClass();
                }

                @Override
                public Set<AnnotatedConstructor<T>> getConstructors() {
                    return annotatedType.getConstructors();
                }

                @Override
                public Set<AnnotatedMethod<? super T>> getMethods() {
                    return annotatedType.getMethods();
                }

                @Override
                public Set<AnnotatedField<? super T>> getFields() {
                    return annotatedType.getFields();
                }

                @Override
                public Type getBaseType() {
                    return annotatedType.getBaseType();
                }

                @Override
                public Set<Type> getTypeClosure() {
                    return annotatedType.getTypeClosure();
                }

                @SuppressWarnings("unchecked")
                @Override
                public <A extends Annotation> A getAnnotation(Class<A> annotationType) {
                    if (AddSwaggerResources.class.equals(annotationType)) {
                        return (A) ADD_SWAGGER_ANNOTATION;
                    }
                    return annotatedType.getAnnotation(annotationType);
                }

                @Override
                public Set<Annotation> getAnnotations() {
                    Set<Annotation> annotations = new HashSet<>(annotatedType.getAnnotations());
                    annotations.add(ADD_SWAGGER_ANNOTATION);
                    return annotations;
                }

                @Override
                public boolean isAnnotationPresent(Class<? extends Annotation> annotationType) {
                    return false;
                }
            });
        }
    }
}
