/*
 * Copyright 2018 Red Hat, Inc, and individual contributors.
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

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import org.eclipse.microprofile.faulttolerance.Asynchronous;
import org.wildfly.swarm.microprofile.faulttolerance.deployment.config.FaultToleranceOperation;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandKey;
import com.netflix.hystrix.HystrixCommandProperties;
import com.netflix.hystrix.HystrixThreadPoolKey;
import com.netflix.hystrix.HystrixThreadPoolProperties;

/**
 * This command is used to wrap any {@link Asynchronous} operation.
 *
 * @author Martin Kouba
 */
public class CompositeCommand extends HystrixCommand<Object> {

    public static Future<Object> createAndQueue(Callable<Object> callable, FaultToleranceOperation operation) {
        return new CompositeCommand(callable, operation).queue();
    }

    private final Callable<Object> callable;

    /**
     *
     * @param callable
     * @param operation
     */
    protected CompositeCommand(Callable<Object> callable, FaultToleranceOperation operation) {
        super(initSetter(operation));
        this.callable = callable;
    }

    @Override
    protected Object run() throws Exception {
        return callable.call();
    }

    private static Setter initSetter(FaultToleranceOperation operation) {
        HystrixCommandProperties.Setter properties = HystrixCommandProperties.Setter();
        HystrixCommandKey commandKey = HystrixCommandKey.Factory
                .asKey(CompositeCommand.class.getSimpleName() + "#" + SimpleCommand.getCommandKey(operation.getMethod()));

        properties.withExecutionIsolationStrategy(HystrixCommandProperties.ExecutionIsolationStrategy.THREAD);
        properties.withFallbackEnabled(false);
        properties.withCircuitBreakerEnabled(false);

        Setter setter = Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey("CompositeCommandGroup")).andCommandKey(commandKey)
                .andCommandPropertiesDefaults(properties);

        // We use a dedicated thread pool for each async operation
        setter.andThreadPoolKey(HystrixThreadPoolKey.Factory.asKey(commandKey.name()));
        HystrixThreadPoolProperties.Setter threadPoolSetter = HystrixThreadPoolProperties.Setter();
        threadPoolSetter.withAllowMaximumSizeToDivergeFromCoreSize(true);
        setter.andThreadPoolPropertiesDefaults(threadPoolSetter);

        return setter;
    }

}
