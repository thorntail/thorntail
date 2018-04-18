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
package io.thorntail.faulttolerance.impl.hystrix;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.PrivilegedActionException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import javax.annotation.Priority;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Unmanaged;
import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

import io.thorntail.faulttolerance.ext.HystrixExtension;
import io.thorntail.faulttolerance.impl.ExecutionContextWithInvocationContext;
import io.thorntail.faulttolerance.impl.FaultToleranceConstants;
import io.thorntail.faulttolerance.impl.FaultToleranceOperation;
import io.thorntail.faulttolerance.impl.config.BulkheadConfig;
import io.thorntail.faulttolerance.impl.config.CircuitBreakerConfig;
import io.thorntail.faulttolerance.impl.config.FallbackConfig;
import io.thorntail.faulttolerance.impl.config.TimeoutConfig;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.faulttolerance.Fallback;
import org.eclipse.microprofile.faulttolerance.FallbackHandler;
import org.eclipse.microprofile.faulttolerance.exceptions.BulkheadException;
import org.eclipse.microprofile.faulttolerance.exceptions.CircuitBreakerOpenException;
import org.eclipse.microprofile.faulttolerance.exceptions.FaultToleranceException;
import org.eclipse.microprofile.faulttolerance.exceptions.TimeoutException;
import org.jboss.logging.Logger;
import io.thorntail.faulttolerance.impl.RetryContext;

import com.netflix.hystrix.HystrixCircuitBreaker;
import com.netflix.hystrix.HystrixCommand.Setter;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandKey;
import com.netflix.hystrix.HystrixCommandProperties;
import com.netflix.hystrix.HystrixThreadPoolKey;
import com.netflix.hystrix.HystrixThreadPoolProperties;
import com.netflix.hystrix.exception.HystrixRuntimeException;

/**
 * @author Antoine Sabot-Durand
 */
@Interceptor
@HystrixCommandBinding
@Priority(Interceptor.Priority.LIBRARY_AFTER + 1)
public class HystrixCommandInterceptor {

    private static final Logger LOGGER = Logger.getLogger(HystrixCommandInterceptor.class);

    @SuppressWarnings("unchecked")
    @Inject
    public HystrixCommandInterceptor(@ConfigProperty(name = "MP_Fault_Tolerance_NonFallback_Enabled", defaultValue = "true") Boolean nonFallBackEnable, @ConfigProperty(name = FaultToleranceConstants.SYNC_CIRCUIT_BREAKER_KEY, defaultValue = "true") Boolean syncCircuitBreakerEnabled, BeanManager beanManager) {
        this.nonFallBackEnable = nonFallBackEnable;
        this.syncCircuitBreakerEnabled = syncCircuitBreakerEnabled;
        this.beanManager = beanManager;
        this.extension = beanManager.getExtension(HystrixExtension.class);
        this.commandMetadataMap = new ConcurrentHashMap<>();
        // WORKAROUND: Hystrix does not allow to use custom HystrixCircuitBreaker impl
        // See also https://github.com/Netflix/Hystrix/issues/9
        try {
            Field field = SecurityActions.getDeclaredField(HystrixCircuitBreaker.Factory.class, "circuitBreakersByCommand");
            SecurityActions.setAccessible(field);
            this.circuitBreakers = (ConcurrentHashMap<String, HystrixCircuitBreaker>) field.get(null);
        } catch (Exception e) {
            throw new IllegalStateException("Could not obtain reference to com.netflix.hystrix.HystrixCircuitBreaker.Factory.circuitBreakersByCommand");
        }
    }

    @AroundInvoke
    public Object interceptCommand(InvocationContext ic) throws Exception {

        Method method = ic.getMethod();
        ExecutionContextWithInvocationContext ctx = new ExecutionContextWithInvocationContext(ic);
        boolean shouldRunCommand = true;
        Object res = null;

        LOGGER.tracef("FT operation intercepted: %s", method);

        CommandMetadata metadata = commandMetadataMap.computeIfAbsent(method, CommandMetadata::new);
        RetryContext retryContext =  nonFallBackEnable && metadata.operation.hasRetry() ? new RetryContext(metadata.operation.getRetry()) : null;
        SynchronousCircuitBreaker syncCircuitBreaker = null;

        while (shouldRunCommand) {
            shouldRunCommand = false;

            if (nonFallBackEnable && syncCircuitBreakerEnabled && metadata.hasCircuitBreaker()) {
                syncCircuitBreaker = getSynchronousCircuitBreaker(metadata.commandKey, metadata.operation.getCircuitBreaker());
            }
            DefaultCommand command = new DefaultCommand(metadata.setter, ctx, metadata.getFallback(ctx), retryContext, metadata.hasCircuitBreaker());

            try {
                if (metadata.operation.isAsync()) {
                    LOGGER.debugf("Queue up command for async execution: %s", metadata.operation);
                    res = new AsyncFuture(command.queue());
                } else {
                    LOGGER.debugf("Sync execution: %s]", metadata.operation);
                    res = command.execute();
                }
                if (syncCircuitBreaker != null) {
                    syncCircuitBreaker.executionSucceeded();
                }
            } catch (HystrixRuntimeException e) {
                if (syncCircuitBreaker != null) {
                    syncCircuitBreaker.executionFailed();
                }
                HystrixRuntimeException.FailureType failureType = e.getFailureType();
                LOGGER.tracef("Hystrix runtime failure [%s] when invoking %s", failureType, method);
                switch (failureType) {
                    case TIMEOUT:
                        TimeoutException timeoutException = new TimeoutException(e);
                        if (retryContext != null && retryContext.shouldRetry()) {
                            shouldRunCommand = shouldRetry(retryContext, timeoutException);
                            if (shouldRunCommand) {
                                continue;
                            }
                        }
                        throw timeoutException;
                    case SHORTCIRCUIT:
                        throw new CircuitBreakerOpenException(method.getName());
                    case REJECTED_THREAD_EXECUTION:
                    case REJECTED_SEMAPHORE_EXECUTION:
                    case REJECTED_SEMAPHORE_FALLBACK:
                        BulkheadException bulkheadException = new BulkheadException(e);
                        if (retryContext != null && retryContext.shouldRetry()) {
                            shouldRunCommand = shouldRetry(retryContext, bulkheadException);
                            if (shouldRunCommand) {
                                continue;
                            }
                        }
                        throw bulkheadException;
                    case COMMAND_EXCEPTION:
                        if (retryContext != null && retryContext.shouldRetry()) {
                            shouldRunCommand = shouldRetry(retryContext, e);
                            continue;
                        }
                    default:
                        throw (e.getCause() instanceof Exception) ? (Exception) e.getCause() : e;
                }
            }
        }
        return res;
    }

    private SynchronousCircuitBreaker getSynchronousCircuitBreaker(HystrixCommandKey commandKey, CircuitBreakerConfig config) {
        HystrixCircuitBreaker circuitBreaker = circuitBreakers.computeIfAbsent(commandKey.name(), (key) -> new SynchronousCircuitBreaker(config));
        if (circuitBreaker instanceof SynchronousCircuitBreaker) {
            return (SynchronousCircuitBreaker) circuitBreaker;
        }
        throw new IllegalStateException("Cached circuit breaker does not extend SynchronousCircuitBreaker");
    }

    private Unmanaged<FallbackHandler<?>> initUnmanaged(FaultToleranceOperation operation) {
        if (operation.hasFallback()) {
            return new Unmanaged<>(beanManager, operation.getFallback().get(FallbackConfig.VALUE));
        }
        return null;
    }

    private Setter initSetter(HystrixCommandKey commandKey, Method method, FaultToleranceOperation operation) {
        HystrixCommandProperties.Setter propertiesSetter = HystrixCommandProperties.Setter();

        if (operation.isAsync()) {
            propertiesSetter.withExecutionIsolationStrategy(HystrixCommandProperties.ExecutionIsolationStrategy.THREAD);
        } else {
            propertiesSetter.withExecutionIsolationStrategy(HystrixCommandProperties.ExecutionIsolationStrategy.SEMAPHORE);
        }

        if (nonFallBackEnable && operation.hasTimeout()) {
            Long value = Duration.of(operation.getTimeout().get(TimeoutConfig.VALUE), operation.getTimeout().get(TimeoutConfig.UNIT)).toMillis();
            if (value > Integer.MAX_VALUE) {
                LOGGER.warnf("Max supported value for @Timeout.value() is %s", Integer.MAX_VALUE);
                value = Long.valueOf(Integer.MAX_VALUE);
            }
            propertiesSetter.withExecutionTimeoutInMilliseconds(value.intValue());
        } else {
            propertiesSetter.withExecutionTimeoutEnabled(false);
        }

        if (nonFallBackEnable && operation.hasCircuitBreaker()) {
            propertiesSetter.withCircuitBreakerEnabled(true)
                    .withCircuitBreakerRequestVolumeThreshold(operation.getCircuitBreaker().get(CircuitBreakerConfig.REQUEST_VOLUME_THRESHOLD))
                    .withCircuitBreakerErrorThresholdPercentage(
                            new Double((Double) operation.getCircuitBreaker().get(CircuitBreakerConfig.FAILURE_RATIO) * 100).intValue())
                    .withCircuitBreakerSleepWindowInMilliseconds((int) Duration
                            .of(operation.getCircuitBreaker().get(CircuitBreakerConfig.DELAY), operation.getCircuitBreaker().get(CircuitBreakerConfig.DELAY_UNIT)).toMillis());
        } else {
            propertiesSetter.withCircuitBreakerEnabled(false);
        }

        Setter setter = Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey("DefaultCommandGroup"))
                // Each method must have a unique command key
                .andCommandKey(commandKey).andCommandPropertiesDefaults(propertiesSetter);

        if (nonFallBackEnable && operation.hasBulkhead()) {
            // TODO: these options need further review
            BulkheadConfig bulkhead = operation.getBulkhead();
            propertiesSetter.withExecutionIsolationSemaphoreMaxConcurrentRequests(bulkhead.get(BulkheadConfig.VALUE));
            propertiesSetter.withExecutionIsolationThreadInterruptOnFutureCancel(true);
            // Each bulkhead policy needs a dedicated thread pool
            setter.andThreadPoolKey(HystrixThreadPoolKey.Factory.asKey(commandKey.name()));
            HystrixThreadPoolProperties.Setter threadPoolSetter = HystrixThreadPoolProperties.Setter();
            threadPoolSetter.withAllowMaximumSizeToDivergeFromCoreSize(true);
            threadPoolSetter.withCoreSize(bulkhead.get(BulkheadConfig.VALUE));
            threadPoolSetter.withMaximumSize(bulkhead.get(BulkheadConfig.VALUE));
            threadPoolSetter.withMaxQueueSize(bulkhead.get(BulkheadConfig.WAITING_TASK_QUEUE));
            threadPoolSetter.withQueueSizeRejectionThreshold(bulkhead.get(BulkheadConfig.WAITING_TASK_QUEUE));
            setter.andThreadPoolPropertiesDefaults(threadPoolSetter);
        }
        return setter;
    }

    private boolean shouldRetry(RetryContext retryContext, Exception e) throws Exception {
        // Decrement the retry count for this attempt
        retryContext.doRetry();
        // Check the exception type
        if (retryContext.shouldRetryOn(e, System.nanoTime())) {
            retryContext.delayIfNeeded();
            return true;
        } else {
            throw e;
        }
    }

    private final ConcurrentHashMap<String, HystrixCircuitBreaker> circuitBreakers;

    private final Map<Method, CommandMetadata> commandMetadataMap;

    private final Boolean nonFallBackEnable;

    private final Boolean syncCircuitBreakerEnabled;

    private final BeanManager beanManager;

    private final HystrixExtension extension;

    private class CommandMetadata {

        public CommandMetadata(Method method) {

            String methodKey = method.toGenericString();

            FaultToleranceOperation operation = null;
            if (extension != null) {
                operation = extension.getFaultToleranceOperation(methodKey);
            }
            if (operation == null) {
                // This is not a bean method - create metadata on the fly
                operation = FaultToleranceOperation.of(method);
                operation.validate();
            }
            this.operation = operation;

            // Initialize Hystrix command setter
            commandKey = HystrixCommandKey.Factory.asKey(methodKey);
            setter = initSetter(commandKey, method, operation);

            if (operation.hasFallback()) {
                FallbackConfig fallbackConfig = operation.getFallback();
                if (!fallbackConfig.get(FallbackConfig.VALUE).equals(Fallback.DEFAULT.class)) {
                    unmanaged = initUnmanaged(operation);
                    fallbackMethod = null;
                } else {
                    unmanaged = null;
                    String fallbackMethodName = fallbackConfig.get(FallbackConfig.FALLBACK_METHOD);
                    if (!"".equals(fallbackMethodName)) {
                        try {
                            fallbackMethod = SecurityActions.getDeclaredMethod(method.getDeclaringClass(), fallbackMethodName, method.getParameterTypes());
                            SecurityActions.setAccessible(fallbackMethod);
                        } catch (NoSuchMethodException | PrivilegedActionException e) {
                            throw new FaultToleranceException("Could not obtain fallback method", e);
                        }
                    } else {
                        fallbackMethod = null;
                    }
                }
            } else {
                unmanaged = null;
                fallbackMethod = null;
            }
        }

        boolean hasFallback() {
            return unmanaged != null || fallbackMethod != null;
        }

        boolean hasCircuitBreaker() {
            return operation.hasCircuitBreaker();
        }

        Supplier<Object> getFallback(ExecutionContextWithInvocationContext ctx) {
            if (!hasFallback()) {
                return null;
            } else if (unmanaged != null) {
                return () -> {
                    Unmanaged.UnmanagedInstance<FallbackHandler<?>> unmanagedInstance = unmanaged.newInstance();
                    FallbackHandler<?> handler = unmanagedInstance.produce().inject().postConstruct().get();
                    try {
                        return handler.handle(ctx);
                    } finally {
                        // The instance exists to service a single invocation only
                        unmanagedInstance.preDestroy().dispose();
                    }
                };
            } else {
                return () -> {
                    try {
                        return fallbackMethod.invoke(ctx.getTarget(), ctx.getParameters());
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        throw new FaultToleranceException("Error during fallback method invocation", e);
                    }
                };
            }
        }

        private final Setter setter;

        private final HystrixCommandKey commandKey;

        private final Unmanaged<FallbackHandler<?>> unmanaged;

        private final Method fallbackMethod;

        private final FaultToleranceOperation operation;

    }

    class AsyncFuture implements Future<Object> {

        private final Future<Object> delegate;

        public AsyncFuture(Future<Object> delegate) {
            this.delegate = delegate;
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            return delegate.cancel(mayInterruptIfRunning);
        }

        @Override
        public boolean isCancelled() {
            return delegate.isCancelled();
        }

        @Override
        public boolean isDone() {
            return delegate.isDone();
        }

        @Override
        public Object get() throws InterruptedException, ExecutionException {
            return unwrap(null, null);
        }

        @Override
        public Object get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, java.util.concurrent.TimeoutException {
            return unwrap(timeout, unit);
        }

        @SuppressWarnings("unchecked")
        private Object unwrap(Long timeout, TimeUnit unit) throws InterruptedException, ExecutionException {
            Object res = delegate.get();
            // For async invocations we need to unwrap the result
            if (res instanceof Future) {
                try {
                    Future<Object> future = (Future<Object>) res;
                    LOGGER.tracef("Unwrapping async result from: %s", future);
                    Object unwrapped = timeout != null ? future.get(timeout, unit) : future.get();
                    LOGGER.tracef("Unwrapped aync result: %s", unwrapped);
                    return unwrapped;
                } catch (Exception e) {
                    throw new IllegalStateException("Unable to get the result of: " + res);
                }
            } else {
                throw new IllegalStateException("A result of an @Asynchronous call must be Future: " + res);
            }
        }

    }

}
