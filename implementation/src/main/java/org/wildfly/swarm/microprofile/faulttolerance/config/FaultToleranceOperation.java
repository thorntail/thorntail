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
package org.wildfly.swarm.microprofile.faulttolerance.config;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.concurrent.Future;
import java.util.function.Function;

import javax.enterprise.inject.spi.AnnotatedMethod;

import org.eclipse.microprofile.faulttolerance.Asynchronous;
import org.eclipse.microprofile.faulttolerance.Bulkhead;
import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.eclipse.microprofile.faulttolerance.Fallback;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.faulttolerance.Timeout;
import org.eclipse.microprofile.faulttolerance.exceptions.FaultToleranceDefinitionException;

/**
 * Fault tolerance operation metadata.
 *
 * @author Martin Kouba
 */
public class FaultToleranceOperation {

    public static FaultToleranceOperation of(AnnotatedMethod<?> annotatedMethod) {
        return new FaultToleranceOperation(annotatedMethod.getJavaMember(),
                isAnnotated(Asynchronous.class, annotatedMethod),
                getConfig(Bulkhead.class, annotatedMethod, BulkheadConfig::new),
                getConfig(CircuitBreaker.class, annotatedMethod, CircuitBreakerConfig::new),
                getConfig(Fallback.class, annotatedMethod, FallbackConfig::new),
                getConfig(Retry.class, annotatedMethod, RetryConfig::new),
                getConfig(Timeout.class, annotatedMethod, TimeoutConfig::new));
    }

    public static FaultToleranceOperation of(Method method) {
        return new FaultToleranceOperation(method,
                isAnnotated(Asynchronous.class, method),
                getConfig(Bulkhead.class, method, BulkheadConfig::new),
                getConfig(CircuitBreaker.class, method, CircuitBreakerConfig::new),
                getConfig(Fallback.class, method, FallbackConfig::new),
                getConfig(Retry.class, method, RetryConfig::new),
                getConfig(Timeout.class, method, TimeoutConfig::new));
    }

    private final Method method;

    private final boolean async;

    private final BulkheadConfig bulkhead;

    private final CircuitBreakerConfig circuitBreaker;

    private final FallbackConfig fallback;

    private final RetryConfig retry;

    private final TimeoutConfig timeout;

    private FaultToleranceOperation(Method method, boolean async, BulkheadConfig bulkhead, CircuitBreakerConfig circuitBreaker, FallbackConfig fallback, RetryConfig retry,
            TimeoutConfig timeout) {
        this.method = method;
        this.async = async;
        this.bulkhead = bulkhead;
        this.circuitBreaker = circuitBreaker;
        this.fallback = fallback;
        this.retry = retry;
        this.timeout = timeout;
    }

    public boolean isAsync() {
        return async;
    }

    public boolean hasBulkhead() {
        return bulkhead != null;
    }

    public BulkheadConfig getBulkhead() {
        return bulkhead;
    }

    public CircuitBreakerConfig getCircuitBreaker() {
        return circuitBreaker;
    }

    public boolean hasCircuitBreaker() {
        return circuitBreaker != null;
    }

    public FallbackConfig getFallback() {
        return fallback;
    }

    public boolean hasFallback() {
        return fallback != null;
    }

    public RetryConfig getRetry() {
        return retry;
    }

    public boolean hasRetry() {
        return retry != null;
    }

    public TimeoutConfig getTimeout() {
        return timeout;
    }

    public boolean hasTimeout() {
        return timeout != null;
    }

    public boolean isLegitimate() {
        return async || bulkhead != null || circuitBreaker != null || fallback != null || retry != null || timeout != null;
    }

    /**
     * Throws {@link FaultToleranceDefinitionException} if validation fails.
     */
    public boolean validate() {
        if (async && !Future.class.equals(method.getReturnType())) {
            throw new FaultToleranceDefinitionException("Invalid @Asynchronous on " + method + " : the return type must be java.util.concurrent.Future");
        }
        if (bulkhead != null) {
            bulkhead.validate();
        }
        if (circuitBreaker != null) {
            circuitBreaker.validate();
        }
        if (fallback != null) {
            fallback.validate();
        }
        if (retry != null) {
            retry.validate();
        }
        if (timeout != null) {
            timeout.validate();
        }
        return true;
    }

    @Override
    public String toString() {
        return "FaultToleranceOperation [method=" + method.toGenericString() + "]";
    }

    private static <A extends Annotation, C extends GenericConfig<A>> C getConfig(Class<A> annotationType, AnnotatedMethod<?> annotatedMethod,
            Function<AnnotatedMethod<?>, C> function) {
        if (isAnnotated(annotationType, annotatedMethod)) {
            return function.apply(annotatedMethod);
        }
        return null;
    }

    private static <A extends Annotation> boolean isAnnotated(Class<A> annotationType, AnnotatedMethod<?> annotatedMethod) {
        return annotatedMethod.isAnnotationPresent(annotationType) || annotatedMethod.getDeclaringType().isAnnotationPresent(annotationType);
    }

    private static <A extends Annotation, C extends GenericConfig<A>> C getConfig(Class<A> annotationType, Method method,
            Function<Method, C> function) {
        if (isAnnotated(annotationType, method)) {
            return function.apply(method);
        }
        return null;
    }

    private static <A extends Annotation> boolean isAnnotated(Class<A> annotationType, Method method) {
        return method.isAnnotationPresent(annotationType) || method.getDeclaringClass().isAnnotationPresent(annotationType);
    }

}
