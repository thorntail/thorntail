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

import java.time.Duration;
import java.util.Arrays;
import java.util.concurrent.Future;
import java.util.function.Supplier;

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
     * @param retryContext
     * @param isAsync
     * @param hasCircuitBreaker
     */
    protected DefaultCommand(Setter setter, ExecutionContextWithInvocationContext ctx, Supplier<Object> fallback, RetryContext retryContext, boolean isAsync, boolean hasCircuitBreaker) {
        super(setter);
        this.ctx = ctx;
        this.fallback = fallback;
        this.retryContext = retryContext;
        this.hasCircuitBreaker = hasCircuitBreaker;
        this.isAsync = isAsync;
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
     * @return the run result
     * @throws Exception on execution failure
     */
    protected Object runWithRetry() throws Exception {
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
                    if (Arrays.stream(retryContext.getAbortOn()).noneMatch(ex -> ex.isAssignableFrom(e.getClass()))
                            && (retryContext.getRetryOn().length == 0 || Arrays.stream(retryContext.getRetryOn()).anyMatch(ex -> ex.isAssignableFrom(e.getClass())))
                            && retryContext.shouldRetry()
                            && System.nanoTime() - retryContext.getStart() <= retryContext.getMaxDuration()) {
                        Long jitterBase = retryContext.getJitter();
                        if (retryContext.getDelay() > 0) {
                            long jitter = (long) (Math.random() * ((jitterBase * 2) + 1)) - jitterBase; // random number between -jitter and +jitter
                            Thread.sleep(retryContext.getDelay() + Duration.of(jitter, retryContext.getJitterDelayUnit()).toMillis());
                        }
                        continue;
                    } else {
                        throw e;
                    }
                }
            }
        }

        return res;
    }

    private Object basicRun() throws Exception {
        Object res;
        res = ctx.proceed();
        return unwrap(res);
    }

    @Override
    protected Object getFallback() {
        if (fallback == null) {
            return super.getFallback();
        }
        return unwrap(fallback.get());
    }

    @SuppressWarnings("rawtypes")
    private Object unwrap(Object res) {
        if (!isAsync) {
            return res;
        }
        // For an async invocation we have to unwrap the result
        if (res instanceof Future) {
            try {
                return ((Future) res).get();
            } catch (Exception e) {
                throw new IllegalStateException("Unable to get the result of: " + res);
            }
        } else {
            throw new IllegalStateException("A result of an @Asynchronous call must be Future: " + res);
        }
    }

    private final Supplier<Object> fallback;

    private final ExecutionContextWithInvocationContext ctx;

    private final RetryContext retryContext;

    private final boolean hasCircuitBreaker;

    private final boolean isAsync;
}
