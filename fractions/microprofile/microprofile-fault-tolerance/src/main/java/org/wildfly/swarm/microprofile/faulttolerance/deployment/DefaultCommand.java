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

import static org.wildfly.swarm.microprofile.faulttolerance.deployment.config.CircuitBreakerConfig.FAIL_ON;

import java.util.function.Supplier;

import org.wildfly.swarm.microprofile.faulttolerance.deployment.config.FaultToleranceOperation;

import com.netflix.hystrix.HystrixCommand;

/**
 * @author Antoine Sabot-Durand
 */
public class DefaultCommand extends HystrixCommand<Object> {

    /**
     *
     * @param setter
     * @param ctx
     * @param fallback
     * @param operation
     * @param retryContext
     */
    protected DefaultCommand(Setter setter, ExecutionContextWithInvocationContext ctx, Supplier<Object> fallback, FaultToleranceOperation operation,
            RetryContext retryContext) {
        super(setter);
        this.ctx = ctx;
        this.fallback = fallback;
        this.failure = null;
        this.operation = operation;
        this.retryContext = retryContext;
    }

    @Override
    protected Object run() throws Exception {
        while (true) {
            try {
                return ctx.proceed();
            } catch (Throwable e) {
                this.failure = e;
                // If there is an async retry context try again
                if (isAsyncRetry() && retryContext != null && retryContext.shouldRetry() && retryContext.nextRetry(e)) {
                    continue;
                }
                throw e;
            }
        }
    }

    @Override
    protected Object getFallback() {
        if (failure != null && ((operation.hasCircuitBreaker() && !isFailureAssignableFromAnyFailureException())
                || (isAsyncRetry() && fallback == null))) {
            // Command failed but the fallback should not be used
            throw new FailureNotHandledException(failure);
        }
        if (fallback == null) {
            return super.getFallback();
        }
        return fallback.get();
    }

    boolean hasFailure() {
        return failure != null;
    }

    private boolean isFailureAssignableFromAnyFailureException() {
        Class<?>[] exceptions = operation.getCircuitBreaker().<Class<?>[]>get(FAIL_ON);
        for (Class<?> exception : exceptions) {
            if (exception.isAssignableFrom(failure.getClass())) {
                return true;
            }
        }
        return false;
    }

    private boolean isAsyncRetry() {
        return operation.hasRetry() && operation.isAsync();
    }

    private Throwable failure;

    private final FaultToleranceOperation operation;

    private final Supplier<Object> fallback;

    private final ExecutionContextWithInvocationContext ctx;

    private final RetryContext retryContext;

}
