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

package org.wildfly.swarm.microprofile.faulttolerance.deployment;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.PrivilegedActionException;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.annotation.Priority;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Unmanaged;
import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.faulttolerance.Asynchronous;
import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.eclipse.microprofile.faulttolerance.Fallback;
import org.eclipse.microprofile.faulttolerance.FallbackHandler;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.faulttolerance.exceptions.BulkheadException;
import org.eclipse.microprofile.faulttolerance.exceptions.CircuitBreakerOpenException;
import org.eclipse.microprofile.faulttolerance.exceptions.FaultToleranceException;
import org.eclipse.microprofile.faulttolerance.exceptions.TimeoutException;
import org.jboss.logging.Logger;
import org.wildfly.swarm.microprofile.faulttolerance.MicroProfileFaultToleranceFraction;
import org.wildfly.swarm.microprofile.faulttolerance.deployment.config.BulkheadConfig;
import org.wildfly.swarm.microprofile.faulttolerance.deployment.config.CircuitBreakerConfig;
import org.wildfly.swarm.microprofile.faulttolerance.deployment.config.FallbackConfig;
import org.wildfly.swarm.microprofile.faulttolerance.deployment.config.FaultToleranceOperation;
import org.wildfly.swarm.microprofile.faulttolerance.deployment.config.TimeoutConfig;

import com.netflix.hystrix.HystrixCircuitBreaker;
import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommand.Setter;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandKey;
import com.netflix.hystrix.HystrixCommandProperties;
import com.netflix.hystrix.HystrixThreadPoolKey;
import com.netflix.hystrix.HystrixThreadPoolProperties;
import com.netflix.hystrix.exception.HystrixRuntimeException;
import com.netflix.hystrix.exception.HystrixRuntimeException.FailureType;

/**
 * <h2>Implementation notes:</h2>
 * <p>
 * If {@link SynchronousCircuitBreaker} is used it is not possible to track the execution inside a Hystrix command because a {@link TimeoutException} should be
 * always counted as a failure, even if the command execution completes normally.
 * </p>
 * <p>
 * We never use {@link HystrixCommand#queue()} for async execution. Mostly to workaround various problems of {@link Asynchronous} {@link Retry} combination. Instead, we
 * create a composite command and inside its run() method we execute commands synchronously.
 * </p>
 *
 * @author Antoine Sabot-Durand
 * @author Martin Kouba
 */
@Interceptor
@HystrixCommandBinding
@Priority(Interceptor.Priority.LIBRARY_AFTER + 1)
public class HystrixCommandInterceptor {

    /**
     * This config property key can be used to disable synchronous circuit breaker functionality. If disabled, {@link CircuitBreaker#successThreshold()} of
     * value greater than 1 is not supported.
     * <p>
     * Moreover, circuit breaker does not necessarily transition from CLOSED to OPEN immediately when a fault tolerance operation completes. See also
     * <a href="https://github.com/Netflix/Hystrix/wiki/Configuration#metrics.healthSnapshot.intervalInMilliseconds">Hystrix configuration</a>
     * </p>
     * <p>
     * In general, application developers are encouraged to disable this feature on high-volume circuits and in production environments.
     * </p>
     */
    public static final String SYNC_CIRCUIT_BREAKER_KEY = "org_wildfly_swarm_microprofile_faulttolerance_syncCircuitBreaker";

    private static final Logger LOGGER = Logger.getLogger(HystrixCommandInterceptor.class);

    @SuppressWarnings("unchecked")
    @Inject
    public HystrixCommandInterceptor(@ConfigProperty(name = "MP_Fault_Tolerance_NonFallback_Enabled", defaultValue = "true") Boolean nonFallBackEnable,
            Config config, Instance<MicroProfileFaultToleranceFraction> fraction, BeanManager beanManager) {
        this.nonFallBackEnable = nonFallBackEnable;
        Optional<Boolean> mpSyncCircuitBreaker = config.getOptionalValue(SYNC_CIRCUIT_BREAKER_KEY, Boolean.class);
        this.syncCircuitBreakerEnabled = mpSyncCircuitBreaker.orElse(fraction.isUnsatisfied() ? true : fraction.get().isSynchronousCircuitBreakerEnabled());
        this.beanManager = beanManager;
        this.extension = beanManager.getExtension(HystrixExtension.class);
        this.commandMetadataMap = new ConcurrentHashMap<>();
        // WORKAROUND: Hystrix does not allow to use custom HystrixCircuitBreaker impl
        // See also https://github.com/Netflix/Hystrix/issues/9
        try {
            Field field = SecurityActions.getDeclaredField(com.netflix.hystrix.HystrixCircuitBreaker.Factory.class, "circuitBreakersByCommand");
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
        LOGGER.tracef("FT operation intercepted: %s", method);

        CommandMetadata metadata = commandMetadataMap.computeIfAbsent(method, CommandMetadata::new);
        RetryContext retryContext = nonFallBackEnable && metadata.operation.hasRetry() ? new RetryContext(metadata.operation.getRetry()) : null;
        SynchronousCircuitBreaker syncCircuitBreaker = getSynchronousCircuitBreaker(metadata);
        Function<Supplier<Object>, SimpleCommand> commandFactory = (fallback) -> new SimpleCommand(metadata.setter, ctx, fallback, metadata.operation);

        if (metadata.operation.isAsync()) {
            LOGGER.debugf("Queue up command for async execution: %s", metadata.operation);
            return new AsyncFuture(CompositeCommand.createAndQueue(() -> executeCommand(commandFactory, retryContext, metadata, ctx, syncCircuitBreaker), metadata.operation));
        } else {
            LOGGER.debugf("Sync execution: %s]", metadata.operation);
            return executeCommand(commandFactory, retryContext, metadata, ctx, syncCircuitBreaker);
        }
    }

    private static Object executeCommand(Function<Supplier<Object>, SimpleCommand> commandFactory, RetryContext retryContext, CommandMetadata metadata,
            ExecutionContextWithInvocationContext ctx, SynchronousCircuitBreaker syncCircuitBreaker) throws Exception {
        while (true) {
            if (retryContext != null) {
                LOGGER.debugf("Executing %s with %s", metadata.operation, retryContext);
            }
            Supplier<Object> fallback = null;
            if (retryContext == null || retryContext.isLastAttempt()) {
                fallback = metadata.getFallback(ctx);
            }
            SimpleCommand command = commandFactory.apply(fallback);
            try {
                Object res = command.execute();
                if (syncCircuitBreaker != null) {
                    if (command.isFailedExecution()) {
                        syncCircuitBreaker.executionFailed();
                    } else {
                        syncCircuitBreaker.executionSucceeded();
                    }
                }
                return res;
            } catch (HystrixRuntimeException e) {
                Exception res = processHystrixRuntimeException(e, retryContext, metadata.operation.getMethod(), syncCircuitBreaker);
                if (res != null) {
                    throw res;
                }
            }
        }
    }

    private static Exception processHystrixRuntimeException(HystrixRuntimeException e, RetryContext retryContext, Method method, SynchronousCircuitBreaker syncCircuitBreaker) {

        HystrixRuntimeException.FailureType failureType = e.getFailureType();
        LOGGER.tracef("Hystrix runtime failure [%s] with cause %s when invoking %s", failureType, e.getCause(), method);

        // See also SWARM-1933
        Throwable fallbackException = e.getFallbackException();
        if (fallbackException instanceof FailureNotHandledException) {
            // Command failed but the circuit breaker should not be used at all
            FailureNotHandledException failureNotHandledException = (FailureNotHandledException) fallbackException;
            return (Exception) failureNotHandledException.getCause();
        }

        if (syncCircuitBreaker != null) {
            syncCircuitBreaker.executionFailed();
        }

        switch (failureType) {
            case TIMEOUT:
                // Note that TimeoutException should be counted as a failure, even if the command execution completes normally
                TimeoutException timeoutException = new TimeoutException(e);
                if (retryContext != null && retryContext.shouldRetry()) {
                    return retryContext.nextRetry(timeoutException);
                }
                return timeoutException;
            case SHORTCIRCUIT:
                return new CircuitBreakerOpenException(method.getName());
            case REJECTED_THREAD_EXECUTION:
            case REJECTED_SEMAPHORE_EXECUTION:
            case REJECTED_SEMAPHORE_FALLBACK:
                BulkheadException bulkheadException = new BulkheadException(e);
                if (retryContext != null && retryContext.shouldRetry()) {
                    return retryContext.nextRetry(bulkheadException);
                }
                return bulkheadException;
            case COMMAND_EXCEPTION:
                if (retryContext != null && retryContext.shouldRetry()) {
                    return retryContext.nextRetry(getRetryCause(e));
                }
            default:
                return getCause(e);
        }
    }

    private static Throwable getRetryCause(HystrixRuntimeException e) {
        if (e.getCause() instanceof Exception) {
            // For some reason Hystrix also wraps errors
            return e.getCause().getCause() instanceof Error ? e.getCause().getCause() : e.getCause();
        }
        return e;
    }

    private static Exception getCause(HystrixRuntimeException e) {
        return (e.getCause() instanceof Exception) ? (Exception) e.getCause() : e;
    }

    private SynchronousCircuitBreaker getSynchronousCircuitBreaker(CommandMetadata metadata) {
        if (nonFallBackEnable && syncCircuitBreakerEnabled && metadata.hasCircuitBreaker()) {
            HystrixCircuitBreaker circuitBreaker = circuitBreakers.computeIfAbsent(metadata.commandKey.name(),
                    (key) -> new SynchronousCircuitBreaker(metadata.operation.getCircuitBreaker()));
            if (circuitBreaker instanceof SynchronousCircuitBreaker) {
                return (SynchronousCircuitBreaker) circuitBreaker;
            }
            throw new IllegalStateException("Cached circuit breaker does not extend SynchronousCircuitBreaker");
        }
        return null;
    }

    private static IllegalStateException errorProcessingHystrixRuntimeException(HystrixRuntimeException e) {
        return new IllegalStateException("Error during processing hystrix runtime exception", e);
    }

    private Unmanaged<FallbackHandler<?>> initUnmanaged(FaultToleranceOperation operation) {
        if (operation.hasFallback()) {
            return new Unmanaged<>(beanManager, operation.getFallback().get(FallbackConfig.VALUE));
        }
        return null;
    }

    private Setter initSetter(HystrixCommandKey commandKey, Method method, FaultToleranceOperation operation) {
        HystrixCommandProperties.Setter propertiesSetter = HystrixCommandProperties.Setter();

        // Async and timeout operations use THREAD isolation strategy
        if (operation.isAsync() || operation.hasTimeout()) {
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
            propertiesSetter.withExecutionIsolationThreadInterruptOnTimeout(true);
        } else {
            propertiesSetter.withExecutionTimeoutEnabled(false);
        }

        if (nonFallBackEnable && operation.hasCircuitBreaker()) {
            propertiesSetter.withCircuitBreakerEnabled(true)
                    .withCircuitBreakerRequestVolumeThreshold(operation.getCircuitBreaker().get(CircuitBreakerConfig.REQUEST_VOLUME_THRESHOLD))
                    .withCircuitBreakerErrorThresholdPercentage(
                            new Double((Double) operation.getCircuitBreaker().get(CircuitBreakerConfig.FAILURE_RATIO) * 100).intValue())
                    .withCircuitBreakerSleepWindowInMilliseconds((int) Duration.of(operation.getCircuitBreaker().get(CircuitBreakerConfig.DELAY),
                            operation.getCircuitBreaker().get(CircuitBreakerConfig.DELAY_UNIT)).toMillis());
        } else {
            propertiesSetter.withCircuitBreakerEnabled(false);
        }

        Setter setter = Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey("DefaultCommandGroup"))
                // Each method must have a unique command key
                .andCommandKey(commandKey).andCommandPropertiesDefaults(propertiesSetter);

        if (nonFallBackEnable && operation.hasBulkhead()) {
            BulkheadConfig bulkhead = operation.getBulkhead();
            if (operation.isAsync()) {
                // Each bulkhead policy needs a dedicated thread pool
                setter.andThreadPoolKey(HystrixThreadPoolKey.Factory.asKey(commandKey.name()));
                HystrixThreadPoolProperties.Setter threadPoolSetter = HystrixThreadPoolProperties.Setter();
                threadPoolSetter.withAllowMaximumSizeToDivergeFromCoreSize(true);
                threadPoolSetter.withCoreSize(bulkhead.get(BulkheadConfig.VALUE));
                threadPoolSetter.withMaximumSize(bulkhead.get(BulkheadConfig.VALUE));
                threadPoolSetter.withMaxQueueSize(bulkhead.get(BulkheadConfig.WAITING_TASK_QUEUE));
                threadPoolSetter.withQueueSizeRejectionThreshold(bulkhead.get(BulkheadConfig.WAITING_TASK_QUEUE));
                setter.andThreadPoolPropertiesDefaults(threadPoolSetter);
            } else {
                // If used without @Asynchronous, the semaphore isolation approach must be used
                propertiesSetter.withExecutionIsolationStrategy(HystrixCommandProperties.ExecutionIsolationStrategy.SEMAPHORE);
                propertiesSetter.withExecutionIsolationSemaphoreMaxConcurrentRequests(bulkhead.get(BulkheadConfig.VALUE));
                propertiesSetter.withExecutionIsolationThreadInterruptOnFutureCancel(true);
            }
        }
        return setter;
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
            commandKey = HystrixCommandKey.Factory.asKey(SimpleCommand.getCommandKey(method));
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
            Future<Object> future;
            try {
                future = unwrapFuture(delegate.get());
            } catch (ExecutionException e) {
                throw unwrapExecutionException(e);
            }
            try {
                return logResult(future, future.get());
            } catch (ExecutionException e) {
                // Rethrow if completed exceptionally
                throw e;
            } catch (Exception e) {
                throw unableToUnwrap(future);
            }
        }

        @Override
        public Object get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, java.util.concurrent.TimeoutException {
            Future<Object> future;
            try {
                future = unwrapFuture(delegate.get());
            } catch (ExecutionException e) {
                throw unwrapExecutionException(e);
            }
            try {
                return logResult(future, future.get(timeout, unit));
            } catch (ExecutionException e) {
                // Rethrow if completed exceptionally
                throw e;
            } catch (Exception e) {
                throw unableToUnwrap(future);
            }
        }

        @SuppressWarnings("unchecked")
        private Future<Object> unwrapFuture(Object futureObject) {
            if (futureObject instanceof Future) {
                return (Future<Object>) futureObject;
            } else {
                throw new IllegalStateException("A result of an @Asynchronous call must be Future: " + futureObject);
            }
        }

        private ExecutionException unwrapExecutionException(ExecutionException e) throws ExecutionException {
            if (e.getCause() instanceof HystrixRuntimeException) {
                Exception res;
                HystrixRuntimeException hystrixRuntimeException = (HystrixRuntimeException) e.getCause();
                if (FailureType.COMMAND_EXCEPTION.equals(hystrixRuntimeException.getFailureType())) {
                    res = getCause(hystrixRuntimeException);
                } else {
                    res = errorProcessingHystrixRuntimeException(hystrixRuntimeException);
                }
                return new ExecutionException(res);
            }
            return e;
        }

        private IllegalStateException unableToUnwrap(Future<Object> future) {
            return new IllegalStateException("Unable to get the result of: " + future);
        }

        private Object logResult(Future<Object> future, Object unwrapped) {
            LOGGER.tracef("Unwrapped async result from %s: %s", future, unwrapped);
            return unwrapped;
        }

    }

}
