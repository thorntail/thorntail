/*
 * Copyright 2017 Red Hat, Inc, and individual contributors.
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
package org.wildfly.swarm.microprofile.faulttolerance.config.extension;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.Set;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AnnotatedConstructor;
import javax.enterprise.inject.spi.AnnotatedField;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.enterprise.util.AnnotationLiteral;

import org.eclipse.microprofile.faulttolerance.Retry;

public class CustomExtension implements Extension {

    void configureService(@Observes ProcessAnnotatedType<UnconfiguredService> event) {
        final AnnotatedType<UnconfiguredService> annotatedType = event.getAnnotatedType();
        event.setAnnotatedType(new AnnotatedType<UnconfiguredService>() {

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
            public <T extends Annotation> T getAnnotation(Class<T> annotationType) {
                if (Retry.class.equals(annotationType)) {
                    return (T) new RetryLiteral();
                }
                return annotatedType.getAnnotation(annotationType);
            }

            @Override
            public Set<Annotation> getAnnotations() {
                Set<Annotation> annotations = new HashSet<>(annotatedType.getAnnotations());
                annotations.add(new RetryLiteral());
                return annotations;
            }

            @Override
            public boolean isAnnotationPresent(Class<? extends Annotation> annotationType) {
                if (Retry.class.equals(annotationType)) {
                    return true;
                }
                return annotatedType.isAnnotationPresent(annotationType);
            }

            @Override
            public Class<UnconfiguredService> getJavaClass() {
                return annotatedType.getJavaClass();
            }

            @Override
            public Set<AnnotatedConstructor<UnconfiguredService>> getConstructors() {
                return annotatedType.getConstructors();
            }

            @Override
            public Set<AnnotatedMethod<? super UnconfiguredService>> getMethods() {
                return annotatedType.getMethods();
            }

            @Override
            public Set<AnnotatedField<? super UnconfiguredService>> getFields() {
                return annotatedType.getFields();
            }
        });
    }

    @SuppressWarnings("serial")
    public class RetryLiteral extends AnnotationLiteral<Retry> implements Retry {

        @Override
        public int maxRetries() {
            return 2;
        }

        @Override
        public long delay() {
            return 0;
        }

        @Override
        public ChronoUnit delayUnit() {
            return ChronoUnit.MILLIS;
        }

        @Override
        public long maxDuration() {
            return Long.MAX_VALUE;
        }

        @Override
        public ChronoUnit durationUnit() {
            return ChronoUnit.NANOS;
        }

        @Override
        public long jitter() {
            return 0;
        }

        @Override
        public ChronoUnit jitterDelayUnit() {
            return ChronoUnit.MILLIS;
        }

        @SuppressWarnings("unchecked")
        @Override
        public Class<? extends Throwable>[] retryOn() {
            return new Class[] {Exception.class};
        }

        @SuppressWarnings("unchecked")
        @Override
        public Class<? extends Throwable>[] abortOn() {
            return new Class[] {};
        }

    }

}
