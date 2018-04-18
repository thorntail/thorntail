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

import java.util.function.Supplier;

import com.netflix.hystrix.HystrixCommand;
import io.thorntail.faulttolerance.impl.ExecutionContextWithInvocationContext;
import io.thorntail.faulttolerance.impl.RetryContext;

/**
 * @author Antoine Sabot-Durand
 */
public class DefaultCommand extends HystrixCommand<Object> {

    /**
     * @param setter
     * @param ctx
     * @param fallback
     * @param retryContext
     * @param hasCircuitBreaker
     */
    protected DefaultCommand(Setter setter, ExecutionContextWithInvocationContext ctx, Supplier<Object> fallback, RetryContext retryContext, boolean hasCircuitBreaker) {
        super(setter);
        this.ctx = ctx;
        this.fallback = fallback;
        this.retryContext = retryContext;
        this.hasCircuitBreaker = hasCircuitBreaker;
    }

    @Override
    protected Object run() throws Exception {
        Object res;
        if (hasCircuitBreaker) {
            res = basicRun();
        } else {
            res = runWithRetry();
        }
        return res;
    }

    /**
     * Run and handle the @Retry logic in the execution loop. This only works when a @CircuitBreaker configuraiton
     * does not exist.
     *
     * @return the run result
     * @throws Exception on execution failure
     */
    private Object runWithRetry() throws Exception {
        Object res = null;
        boolean notExecuted = true;
        if (retryContext == null) {
            res = basicRun();
        } else {
            while (notExecuted && retryContext.shouldRetry()) {
                retryContext.doRetry();
                try {
                    res = basicRun();
                    notExecuted = false;
                } catch (Exception e) {
                    if (retryContext.shouldRetryOn(e, System.nanoTime())) {
                        retryContext.delayIfNeeded();
                    } else {
                        throw e;
                    }
                }
            }
        }

        return res;
    }

    private Object basicRun() throws Exception {
        return ctx.proceed();
    }

    @Override
    protected Object getFallback() {
        if (fallback == null) {
            return super.getFallback();
        }
        return fallback.get();
    }

    private final Supplier<Object> fallback;

    private final ExecutionContextWithInvocationContext ctx;

    private final RetryContext retryContext;

    private final boolean hasCircuitBreaker;

}
