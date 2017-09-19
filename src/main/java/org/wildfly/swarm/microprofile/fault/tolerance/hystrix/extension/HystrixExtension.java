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

package org.wildfly.swarm.microprofile.fault.tolerance.hystrix.extension;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.Annotated;
import javax.enterprise.inject.spi.AnnotatedConstructor;
import javax.enterprise.inject.spi.AnnotatedField;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.enterprise.inject.spi.WithAnnotations;

import org.eclipse.microprofile.faulttolerance.Asynchronous;
import org.eclipse.microprofile.faulttolerance.Bulkhead;
import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.eclipse.microprofile.faulttolerance.Fallback;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.faulttolerance.Timeout;
import org.wildfly.swarm.microprofile.fault.tolerance.hystrix.HystrixCommandBinding;
import org.wildfly.swarm.microprofile.fault.tolerance.hystrix.config.BulkheadConfig;
import org.wildfly.swarm.microprofile.fault.tolerance.hystrix.config.CircuitBreakerConfig;
import org.wildfly.swarm.microprofile.fault.tolerance.hystrix.config.FallbackConfig;
import org.wildfly.swarm.microprofile.fault.tolerance.hystrix.config.RetryContext;
import org.wildfly.swarm.microprofile.fault.tolerance.hystrix.config.TimeoutConfig;

/**
 * @author Antoine Sabot-Durand
 */

/**
 * @author Antoine Sabot-Durand
 */
public class HystrixExtension implements Extension {

    void registerInterceptorBindings(@Observes BeforeBeanDiscovery bbd, BeanManager bm) {

        bbd.addInterceptorBinding(new HystrixInterceptorBindingAnnotatedType<>(bm.createAnnotatedType(CircuitBreaker.class)));
        bbd.addInterceptorBinding(new HystrixInterceptorBindingAnnotatedType<>(bm.createAnnotatedType(Retry.class)));
        bbd.addInterceptorBinding(new HystrixInterceptorBindingAnnotatedType<>(bm.createAnnotatedType(Timeout.class)));
        bbd.addInterceptorBinding(new HystrixInterceptorBindingAnnotatedType<>(bm.createAnnotatedType(Asynchronous.class)));
        bbd.addInterceptorBinding(new HystrixInterceptorBindingAnnotatedType<>(bm.createAnnotatedType(Fallback.class)));
        bbd.addInterceptorBinding(new HystrixInterceptorBindingAnnotatedType<>(bm.createAnnotatedType(Bulkhead.class)));
    }



    void validateTimeout(@Observes @WithAnnotations(Timeout.class) ProcessAnnotatedType<?> pat) {
        validate(pat, m -> new TimeoutConfig(m).validate(), Timeout.class);
    }

    void validateRetry(@Observes @WithAnnotations(Retry.class) ProcessAnnotatedType<?> pat) {
        validate(pat, m -> new RetryContext(m).validate(), Retry.class);
    }

    void validateCircuitBreaker(@Observes @WithAnnotations(CircuitBreaker.class) ProcessAnnotatedType<?> pat) {
        validate(pat, m -> new CircuitBreakerConfig(m).validate(), CircuitBreaker.class);
    }

    void validateBulkhead(@Observes @WithAnnotations(Bulkhead.class) ProcessAnnotatedType<?> pat) {
        validate(pat, m -> new BulkheadConfig(m).validate(), Bulkhead.class);
    }

    void validateFallback(@Observes @WithAnnotations(Fallback.class) ProcessAnnotatedType<?> pat) {
        validate(pat, m -> new FallbackConfig(m).validate(), Fallback.class);
    }

    private void validate(ProcessAnnotatedType<?> pat, Consumer<Annotated> validator, Class<? extends Annotation> annotationType) {
        AnnotatedType<?> at =pat.getAnnotatedType();

        if (at.isAnnotationPresent(annotationType)) {
            validator.accept(at);
        }

        at.getMethods().stream()
                .filter(m -> m.isAnnotationPresent(annotationType))
                .forEach(validator);
    }

    public static class HystrixInterceptorBindingAnnotatedType<T extends Annotation> implements AnnotatedType<T> {

        public HystrixInterceptorBindingAnnotatedType(AnnotatedType<T> delegate) {
            this.delegate = delegate;
            annotations = new HashSet<>(delegate.getAnnotations());
            annotations.add(HystrixCommandBinding.Literal.INSTANCE);
        }

        public Class<T> getJavaClass() {
            return delegate.getJavaClass();
        }

        public Set<AnnotatedConstructor<T>> getConstructors() {
            return delegate.getConstructors();
        }

        public Set<AnnotatedMethod<? super T>> getMethods() {
            return delegate.getMethods();
        }

        public Set<AnnotatedField<? super T>> getFields() {
            return delegate.getFields();
        }

        public Type getBaseType() {
            return delegate.getBaseType();
        }

        public Set<Type> getTypeClosure() {
            return delegate.getTypeClosure();
        }

        public <S extends Annotation> S getAnnotation(Class<S> annotationType) {
            if (HystrixCommandBinding.class.equals(annotationType)) {
                return (S) HystrixCommandBinding.Literal.INSTANCE;
            }
            return delegate.getAnnotation(annotationType);
        }

        public Set<Annotation> getAnnotations() {
            return annotations;
        }

        public boolean isAnnotationPresent(Class<? extends Annotation> annotationType) {
            return HystrixCommandBinding.class.equals(annotationType) || delegate.isAnnotationPresent(annotationType);
        }

        private AnnotatedType<T> delegate;

        private Set<Annotation> annotations;
    }


}

