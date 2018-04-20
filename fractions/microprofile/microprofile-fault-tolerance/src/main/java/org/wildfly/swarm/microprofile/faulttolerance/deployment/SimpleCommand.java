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

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.function.Supplier;

import org.wildfly.swarm.microprofile.faulttolerance.deployment.config.FaultToleranceOperation;

import com.netflix.hystrix.HystrixCommand;

/**
 * @author Antoine Sabot-Durand
 * @author Martin Kouba
 */
public class SimpleCommand extends HystrixCommand<Object> {

    public static String getCommandKey(Method method) {
        StringBuilder builder = new StringBuilder();
        builder.append(method.getDeclaringClass().getName().replace(".", "_"));
        builder.append("#");
        builder.append(method.getName());
        builder.append("(");
        Type[] params = method.getGenericParameterTypes();
        for (int j = 0; j < params.length; j++) {
            builder.append(params[j].getTypeName());
            if (j < (params.length - 1)) {
                builder.append(',');
            }
        }
        builder.append(")");
        return builder.toString();
    }

    /**
     *
     * @param setter
     * @param ctx
     * @param fallback
     * @param operation
     */
    protected SimpleCommand(Setter setter, ExecutionContextWithInvocationContext ctx, Supplier<Object> fallback, FaultToleranceOperation operation) {
        super(setter);
        this.ctx = ctx;
        this.fallback = fallback;
        this.operation = operation;
    }

    @Override
    protected Object run() throws Exception {
        return ctx.proceed();
    }

    @Override
    protected Object getFallback() {
        Throwable failure = getFailedExecutionException();
        if (failure != null && operation.hasCircuitBreaker() && !isFailureAssignableFromAnyFailureException(failure)) {
            // Command failed but the fallback should not be used
            throw new FailureNotHandledException(failure);
        }
        if (fallback == null) {
            return super.getFallback();
        }
        return fallback.get();
    }

    private boolean isFailureAssignableFromAnyFailureException(Throwable failure) {
        Class<?>[] exceptions = operation.getCircuitBreaker().<Class<?>[]>get(FAIL_ON);
        for (Class<?> exception : exceptions) {
            if (exception.isAssignableFrom(failure.getClass())) {
                return true;
            }
        }
        return false;
    }

    private final FaultToleranceOperation operation;

    private final Supplier<Object> fallback;

    private final ExecutionContextWithInvocationContext ctx;

}
