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

package org.wildfly.swarm.microprofile.faulttolerance;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.PrivilegedActionException;
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

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.eclipse.microprofile.faulttolerance.Fallback;
import org.eclipse.microprofile.faulttolerance.FallbackHandler;
import org.eclipse.microprofile.faulttolerance.exceptions.CircuitBreakerOpenException;
import org.eclipse.microprofile.faulttolerance.exceptions.FaultToleranceException;
import org.eclipse.microprofile.faulttolerance.exceptions.TimeoutException;
import org.jboss.logging.Logger;
import org.wildfly.swarm.microprofile.faulttolerance.config.BulkheadConfig;
import org.wildfly.swarm.microprofile.faulttolerance.config.CircuitBreakerConfig;
import org.wildfly.swarm.microprofile.faulttolerance.config.FallbackConfig;
import org.wildfly.swarm.microprofile.faulttolerance.config.FaultToleranceOperation;
import org.wildfly.swarm.microprofile.faulttolerance.config.TimeoutConfig;

import com.netflix.hystrix.HystrixCircuitBreaker;
import com.netflix.hystrix.HystrixCommand.Setter;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandKey;
import com.netflix.hystrix.HystrixCommandProperties;
import com.netflix.hystrix.HystrixThreadPoolProperties;
import com.netflix.hystrix.exception.HystrixRuntimeException;

/**
 * @author Antoine Sabot-Durand
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
    public HystrixCommandInterceptor() {
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
        boolean shouldRunCommand = true;
        Object res = null;

        LOGGER.debugf("FT operation intercepted: %s", method);

        CommandMetadata metadata = commandMetadataMap.computeIfAbsent(method, CommandMetadata::new);
        RetryContext retryContext =  nonFallBackEnable && metadata.operation.hasRetry() ? new RetryContext(metadata.operation.getRetry()) : null;
        SynchronousCircuitBreaker syncCircuitBreaker = null;

        while (shouldRunCommand) {
            shouldRunCommand = false;

            if (nonFallBackEnable && syncCircuitBreakerEnabled && metadata.hasCircuitBreaker()) {
                syncCircuitBreaker = getSynchronousCircuitBreaker(metadata.commandKey, metadata.operation.getCircuitBreaker());
            }
            DefaultCommand command = new DefaultCommand(metadata.setter, ctx, metadata.getFallback(ctx), retryContext, metadata.operation.isAsync(),
                    metadata.hasCircuitBreaker());

            try {
                if (metadata.operation.isAsync()) {
                    res = command.queue();
                } else {
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
                    case TIMEOUT: {
                        if (retryContext != null && retryContext.shouldRetry()) {
                            shouldRunCommand = shouldRetry(retryContext, new TimeoutException(e));
                            if (shouldRunCommand) {
                                continue;
                            }
                        }
                        throw new TimeoutException(e);
                    }
                    case SHORTCIRCUIT:
                        throw new CircuitBreakerOpenException(method.getName());
                    case REJECTED_THREAD_EXECUTION:
                    case REJECTED_SEMAPHORE_EXECUTION:
                    case REJECTED_SEMAPHORE_FALLBACK:
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
        HystrixThreadPoolProperties.Setter threadPoolSetter = HystrixThreadPoolProperties.Setter();

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

        if (nonFallBackEnable && operation.hasBulkhead()) {
            propertiesSetter.withExecutionIsolationSemaphoreMaxConcurrentRequests(operation.getBulkhead().get(BulkheadConfig.VALUE))
                    .withExecutionIsolationThreadInterruptOnFutureCancel(true);
            // TODO: review the following comments
            // threadPoolSetter.withCoreSize(conf.get(BulkheadConfig.VALUE));
            // threadPoolSetter.withMaximumSize(conf.get(BulkheadConfig.VALUE));
        }

        return Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey("DefaultCommandGroup"))
                // Each method must have a unique command key
                .andCommandKey(commandKey).andCommandPropertiesDefaults(propertiesSetter).andThreadPoolPropertiesDefaults(threadPoolSetter);
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

    @Inject
    @ConfigProperty(name = "MP_Fault_Tolerance_NonFallback_Enabled", defaultValue = "true")
    private Boolean nonFallBackEnable;

    @Inject
    @ConfigProperty(name = SYNC_CIRCUIT_BREAKER_KEY, defaultValue = "true")
    private Boolean syncCircuitBreakerEnabled;

    @Inject
    private BeanManager beanManager;

    @Inject
    private HystrixExtension extension;

    private class CommandMetadata {

        public CommandMetadata(Method method) {

            String methodKey = method.toGenericString();

            FaultToleranceOperation operation = extension.getFaultToleranceOperation(methodKey);
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

}
