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

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;
import javax.enterprise.concurrent.ManagedThreadFactory;
import javax.enterprise.context.Dependent;

import org.jboss.logging.Logger;

import com.netflix.hystrix.HystrixThreadPoolKey;
import com.netflix.hystrix.HystrixThreadPoolProperties;
import com.netflix.hystrix.strategy.concurrency.HystrixConcurrencyStrategy;
import com.netflix.hystrix.strategy.properties.HystrixProperty;

/**
 * The default concurrency strategy using the managed version of {@link ThreadFactory}.
 *
 * <p>
 * A user is allowed to provide a custom implementation of {@link HystrixConcurrencyStrategy}. The bean should be {@link Dependent}, must be marked as
 * alternative and selected globally for an application.
 * </p>
 *
 * @author Martin Kouba
 */
@Dependent
class DefaultHystrixConcurrencyStrategy extends HystrixConcurrencyStrategy {

    private static final Logger LOGGER = Logger.getLogger(DefaultHystrixConcurrencyStrategy.class);

    @Resource(lookup = "java:comp/DefaultManagedThreadFactory")
    ManagedThreadFactory threadFactory;

    @Override
    public ThreadPoolExecutor getThreadPool(HystrixThreadPoolKey threadPoolKey, HystrixProperty<Integer> corePoolSize, HystrixProperty<Integer> maximumPoolSize,
            HystrixProperty<Integer> keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue) {
        int dynamicCoreSize = corePoolSize.get();
        int dynamicMaximumSize = maximumPoolSize.get();

        LOGGER.debugf("Get thread pool executor for %s [core: %s, max: %s]", threadPoolKey.name(), dynamicCoreSize, dynamicMaximumSize);

        return new ThreadPoolExecutor(dynamicCoreSize, dynamicCoreSize > dynamicMaximumSize ? dynamicCoreSize : dynamicMaximumSize, keepAliveTime.get(), unit,
                workQueue, threadFactory);
    }

    @Override
    public ThreadPoolExecutor getThreadPool(HystrixThreadPoolKey threadPoolKey, HystrixThreadPoolProperties threadPoolProperties) {

        boolean allowMaximumSizeToDivergeFromCoreSize = threadPoolProperties.getAllowMaximumSizeToDivergeFromCoreSize().get();
        int dynamicCoreSize = threadPoolProperties.coreSize().get();
        int dynamicMaximumSize = threadPoolProperties.maximumSize().get();
        int keepAliveTime = threadPoolProperties.keepAliveTimeMinutes().get();
        int maxQueueSize = threadPoolProperties.maxQueueSize().get();
        BlockingQueue<Runnable> workQueue = getBlockingQueue(maxQueueSize);

        LOGGER.debugf("Get thread pool executor for %s [allowMaximumSizeToDivergeFromCoreSize: %s, core: %s, max: %s]", threadPoolKey.name(), allowMaximumSizeToDivergeFromCoreSize,
                dynamicCoreSize, dynamicMaximumSize);

        if (allowMaximumSizeToDivergeFromCoreSize) {
            return new ThreadPoolExecutor(dynamicCoreSize, dynamicCoreSize > dynamicMaximumSize ? dynamicCoreSize : dynamicMaximumSize, keepAliveTime,
                    TimeUnit.MINUTES, workQueue, threadFactory);
        } else {
            return new ThreadPoolExecutor(dynamicCoreSize, dynamicCoreSize, keepAliveTime, TimeUnit.MINUTES, workQueue, threadFactory);
        }
    }

}
