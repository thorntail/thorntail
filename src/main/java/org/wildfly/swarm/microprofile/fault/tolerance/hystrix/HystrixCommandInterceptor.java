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

package org.wildfly.swarm.microprofile.fault.tolerance.hystrix;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import javax.annotation.Priority;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Unmanaged;
import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

import com.netflix.hystrix.HystrixCommand.Setter;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandKey;
import com.netflix.hystrix.HystrixCommandProperties;
import com.netflix.hystrix.HystrixThreadPoolProperties;
import com.netflix.hystrix.exception.HystrixRuntimeException;
import org.eclipse.microprofile.faulttolerance.Asynchronous;
import org.eclipse.microprofile.faulttolerance.Bulkhead;
import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.eclipse.microprofile.faulttolerance.Fallback;
import org.eclipse.microprofile.faulttolerance.FallbackHandler;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.faulttolerance.Timeout;
import org.eclipse.microprofile.faulttolerance.exceptions.FaultToleranceException;
import org.eclipse.microprofile.faulttolerance.exceptions.TimeoutException;
import org.wildfly.swarm.microprofile.fault.tolerance.hystrix.config.BulkheadConfig;
import org.wildfly.swarm.microprofile.fault.tolerance.hystrix.config.CircuitBreakerConfig;
import org.wildfly.swarm.microprofile.fault.tolerance.hystrix.config.FallbackConfig;
import org.wildfly.swarm.microprofile.fault.tolerance.hystrix.config.RetryContext;
import org.wildfly.swarm.microprofile.fault.tolerance.hystrix.config.TimeoutConfig;

/**
 * @author Antoine Sabot-Durand
 */

@Interceptor
@HystrixCommandBinding
@Priority(Interceptor.Priority.LIBRARY_AFTER + 1)
public class HystrixCommandInterceptor {


    @AroundInvoke
    public Object interceptCommand(InvocationContext ic) throws Exception {


        Method method = ic.getMethod();
        ctx = new ExecutionContextWithInvocationContext(ic);

        boolean shouldRunCommand = true;
        Object res = null;


        CommandMetadata metadata = commandMetadataMap.computeIfAbsent(method, CommandMetadata::new);

        Supplier<Object> fallback = metadata.hasFallbackClass() ? () -> {
            Unmanaged.UnmanagedInstance<FallbackHandler<?>> unmanagedInstance = metadata.unmanaged.newInstance();
            FallbackHandler<?> handler = unmanagedInstance.produce().inject().postConstruct().get();
            try {
                return handler.handle(ctx);
            } finally {
                // The instance exists to service a single invocation only
                unmanagedInstance.preDestroy().dispose();
            }
        } : metadata.fallbackSupplier;


        Asynchronous async = getAnnotation(method, Asynchronous.class);

        while (shouldRunCommand) {
            shouldRunCommand = false;
            DefaultCommand command = new DefaultCommand(metadata.setter, ctx::proceed, fallback, metadata.retryContext);


            try {
                if (async != null) {
                    res = command.queue();
                } else {
                    res = command.execute();
                }
            } catch (HystrixRuntimeException e) {
                if (e.getFailureType().equals(HystrixRuntimeException.FailureType.TIMEOUT)) {
                    if (metadata.retryContext != null && metadata.retryContext.getMaxExecNumber() > 0) {
                        //retry.incMaxNumberExec();
                        shouldRunCommand = true;
                        continue;
                    }
                    throw new TimeoutException(e);
                } else {
                    throw e;
                }
            }
        }

        return res;
    }

    private <T extends Annotation> T getAnnotation(Method method, Class<T> annotation) {
        if (method.isAnnotationPresent(annotation)) {
            return method.getAnnotation(annotation);
        } else if (method.getDeclaringClass().isAnnotationPresent(annotation)) {
            return method.getDeclaringClass().getAnnotation(annotation);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private Unmanaged<FallbackHandler<?>> initUnmanaged(Method method) {
        Fallback fallback = getAnnotation(method, Fallback.class);
        if (fallback != null) {
            return (Unmanaged<FallbackHandler<?>>) new Unmanaged<>(beanManager, fallback.value());
        }
        return null;
    }

    private Setter initSetter(Method method) {
        HystrixCommandProperties.Setter propertiesSetter = HystrixCommandProperties.Setter();

        HystrixThreadPoolProperties.Setter threadPoolSetter = HystrixThreadPoolProperties.Setter();

        Timeout timeout = getAnnotation(method, Timeout.class);
        CircuitBreaker circuitBreaker = getAnnotation(method, CircuitBreaker.class);
        Bulkhead bulkhead = getAnnotation(method,Bulkhead.class);

        propertiesSetter.withExecutionIsolationStrategy(HystrixCommandProperties.ExecutionIsolationStrategy.SEMAPHORE);

        if (timeout != null) {
            // TODO: In theory a user might specify a long value
            TimeoutConfig config = new TimeoutConfig(timeout, method);
            propertiesSetter.withExecutionTimeoutInMilliseconds((int) Duration.of(config.get(TimeoutConfig.VALUE), config.get(TimeoutConfig.UNIT)).toMillis());
        } else {
            propertiesSetter.withExecutionTimeoutEnabled(false);
        }

        if (circuitBreaker != null) {

            CircuitBreakerConfig conf = new CircuitBreakerConfig(circuitBreaker, method);
            propertiesSetter.withCircuitBreakerEnabled(true)
                    .withCircuitBreakerRequestVolumeThreshold(conf.get(CircuitBreakerConfig.REQUEST_VOLUME_THRESHOLD))
                    .withCircuitBreakerErrorThresholdPercentage(new Double((Double) conf.get(CircuitBreakerConfig.FAILURE_RATIO) * 100).intValue())
                    .withCircuitBreakerSleepWindowInMilliseconds((int) Duration.of(conf.get(CircuitBreakerConfig.DELAY), conf.get(CircuitBreakerConfig.DELAY_UNIT)).toMillis());
        } else {
            propertiesSetter.withCircuitBreakerEnabled(false);
        }


        if(bulkhead != null) {
            BulkheadConfig conf = new BulkheadConfig(bulkhead,method);
            propertiesSetter.withExecutionIsolationSemaphoreMaxConcurrentRequests(conf.get(BulkheadConfig.VALUE))
            .withExecutionIsolationThreadInterruptOnFutureCancel(true);

            //threadPoolSetter.withCoreSize(conf.get(BulkheadConfig.VALUE));
            //threadPoolSetter.withMaximumSize(conf.get(BulkheadConfig.VALUE));


        }

        return Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey("DefaultCommandGroup"))
                // Each method must have a unique command key
                .andCommandKey(HystrixCommandKey.Factory.asKey(method.getDeclaringClass().getName() + method.toString()))
                .andCommandPropertiesDefaults(propertiesSetter)
                .andThreadPoolPropertiesDefaults(threadPoolSetter);
    }

    private final Map<Method, CommandMetadata> commandMetadataMap = new ConcurrentHashMap<>();

    private ExecutionContextWithInvocationContext ctx;

    @Inject
    private BeanManager beanManager;

    private class CommandMetadata {

        public CommandMetadata(Method method) {
            setter = initSetter(method);

            Fallback fallback = getAnnotation(method, Fallback.class);

            if (fallback != null) {
                FallbackConfig fc = new FallbackConfig(fallback,method);
                if (!fc.get(FallbackConfig.VALUE).equals(Fallback.DEFAULT.class)) {
                    unmanaged = initUnmanaged(method);
                } else {
                    unmanaged = null;
                    if (!"".equals(fc.get(FallbackConfig.FALLBACK_METHOD))) {
                        try {
                            fallbackMethod = method.getDeclaringClass().getMethod(fc.get(FallbackConfig.FALLBACK_METHOD), method.getParameterTypes());
                        } catch (NoSuchMethodException e) {
                            throw new FaultToleranceException("Fallback method not found", e);
                        }
                        fallbackSupplier = () -> {
                            try {
                                return fallbackMethod.invoke(ctx.getTarget(), ctx.getParameters());
                            } catch (IllegalAccessException | InvocationTargetException e) {
                                throw new FaultToleranceException("Error during fallback method invocation", e);
                            }
                        };

                    }
                }
            } else {
                unmanaged = null;
            }

            Retry retry = getAnnotation(method, Retry.class);
            if (retry != null) {
                retryContext = new RetryContext(retry, method);
            } else {
                retryContext = null;
            }

        }

        boolean hasFallbackClass() {
            return unmanaged != null;
        }

        private final Setter setter;

        private final Unmanaged<FallbackHandler<?>> unmanaged;

        private final RetryContext retryContext;

        private Supplier<Object> fallbackSupplier = null;

        private Method fallbackMethod = null;

    }

}
