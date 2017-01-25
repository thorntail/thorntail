/**
 * Copyright 2015-2016 Red Hat, Inc, and individual contributors.
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
package org.wildfly.swarm.netflix.hystrix;

import org.wildfly.swarm.spi.api.Defaultable;
import org.wildfly.swarm.spi.api.Fraction;
import org.wildfly.swarm.spi.api.annotations.Configurable;
import org.wildfly.swarm.spi.api.annotations.DeploymentModule;
import org.wildfly.swarm.spi.api.annotations.DeploymentModules;

import static org.wildfly.swarm.netflix.hystrix.HystrixProperties.DEFAULT_STREAM_PATH;
import static org.wildfly.swarm.spi.api.Defaultable.bool;
import static org.wildfly.swarm.spi.api.Defaultable.integer;
import static org.wildfly.swarm.spi.api.Defaultable.longInteger;
import static org.wildfly.swarm.spi.api.Defaultable.string;

/**
 * @author Ken Finnigan
 */
@DeploymentModules({
        @DeploymentModule(name = "com.netflix.hystrix"),
        @DeploymentModule(name = "io.reactivex.rxjava")
})
public class HystrixFraction implements Fraction<HystrixFraction> {

    public HystrixFraction streamPath(String streamPath) {
        this.streamPath.set(streamPath);
        return this;
    }

    public String streamPath() {
        return this.streamPath.get();
    }

    @Configurable("swarm.hystrix.stream.path")
    private Defaultable<String> streamPath = string(DEFAULT_STREAM_PATH);

    @Configurable("swarm.hystrix.command.default.execution.isolation.strategy")
    private Defaultable<String> isolationStrategy = string("THREAD");

    @Configurable("swarm.hystrix.command.default.execution.isolation.thread.timeoutInMilliseconds")
    private Defaultable<Long> threadTimeoutInMilliseconds = longInteger(1000);

    @Configurable("swarm.hystrix.command.default.execution.timeout.enabled")
    private Defaultable<Boolean> threadTimeoutEnabled = bool(true);

    @Configurable("swarm.hystrix.command.default.execution.isolation.thread.interruptOnTimeout")
    private Defaultable<Boolean> threadInterruptOnTimeout = bool(true);

    @Configurable("swarm.hystrix.command.default.execution.isolation.thread.interruptOnCancel")
    private Defaultable<Boolean> threadInterruptOnCancel = bool(false);

    @Configurable("swarm.hystrix.command.default.execution.isolation.semaphore.maxConcurrentRequests")
    private Defaultable<Integer> semaphorMaxConcurrentRequests = integer(10);

    @Configurable("swarm.hystrix.command.default.fallback.enabled")
    private Defaultable<Boolean> fallbackEnabled = bool(true);

    @Configurable("swarm.hystrix.command.default.circuitBreaker.enabled")
    private Defaultable<Boolean> circuitBreakerEnabled = bool(true);

    @Configurable("swarm.hystrix.command.default.circuitBreaker.requestVolumeThreshold")
    private Defaultable<Integer> circuitBreakerRequestVolumeThreshold = integer(20);

    @Configurable("swarm.hystrix.command.default.circuitBreaker.sleepWindowInMilliseconds")
    private Defaultable<Long> circuitBreakerSleepWindowInMilliseconds = longInteger(5000);

    @Configurable("swarm.hystrix.command.default.circuitBreaker.errorThresholdPercentage")
    private Defaultable<Integer> circuitBreakererrorThresholdPercentage = integer(50);

    @Configurable("swarm.hystrix.command.default.circuitBreaker.forceClosed")
    private Defaultable<Boolean> circuitBreakerForceClosed = bool(false);

    @Configurable("swarm.hystrix.command.default.metrics.rollingStats.timeInMilliseconds")
    private Defaultable<Long> rollingStatsTimeInMilliseconds = longInteger(10000);

    @Configurable("swarm.hystrix.command.default.metrics.rollingStats.numBuckets")
    private Defaultable<Integer> rollingStatsNumBuckets = integer(10);

    @Configurable("swarm.hystrix.command.default.metrics.rollingPercentile.enabled")
    private Defaultable<Boolean> rollingPercentileEnabled = bool(true);

    @Configurable("swarm.hystrix.command.default.metrics.rollingPercentile.timeInMilliseconds")
    private Defaultable<Long> rollingPercentileTimeInMilliseconds = longInteger(60000);

    @Configurable("swarm.hystrix.command.default.metrics.rollingPercentile.numBuckets")
    private Defaultable<Integer> rollingPercentileNumBuckets = integer(6);

    @Configurable("swarm.hystrix.command.default.metrics.rollingPercentile.bucketSize")
    private Defaultable<Integer> rollingPercentileBucketSize = integer(100);

    @Configurable("swarm.hystrix.command.default.metrics.healthSnapshot.intervalInMilliseconds")
    private Defaultable<Long> healthSnapshotIntervalInMilliseconds = longInteger(500);

    @Configurable("swarm.hystrix.command.default.requestCache.enabled")
    private Defaultable<Boolean> requestCacheEnabled = bool(true);

    @Configurable("swarm.hystrix.command.default.requestLog.enabled")
    private Defaultable<Boolean> requestLogEnabled = bool(true);

    @Configurable("swarm.hystrix.collapser.default.maxRequestsInBatch")
    private Defaultable<Integer> maxRequestsInBatch = integer(Integer.MAX_VALUE);

    @Configurable("swarm.hystrix.collapser.default.timerDelayInMilliseconds")
    private Defaultable<Long> timerDelayInMilliseconds = longInteger(10);

    @Configurable("swarm.hystrix.collapser.default.requestCache.enabled")
    private Defaultable<Boolean> collapserRequestCacheEnabled = bool(true);

    @Configurable("swarm.hystrix.threadpool.default.coreSize")
    private Defaultable<Integer> threadpoolCoreSize = integer(10);

    @Configurable("swarm.hystrix.threadpool.default.maximumSize")
    private Defaultable<Integer> threadpoolMaximumSize = integer(10);

    @Configurable("swarm.hystrix.threadpool.default.maxQueueSize")
    private Defaultable<Integer> threadpoolMaxQueueSize = integer(-1);

    @Configurable("swarm.hystrix.threadpool.default.queueSizeRejectionThreshold")
    private Defaultable<Integer> threadpoolQueueSizeRejectionThreshold = integer(5);

    @Configurable("swarm.hystrix.threadpool.default.keepAliveTimeMinutes")
    private Defaultable<Integer> threadpoolKeepAliveTimeMinutes = integer(1);

    @Configurable("swarm.hystrix.threadpool.default.allowMaximumSizeToDivergeFromCoreSize")
    private Defaultable<Boolean> threadpoolKeepAllowMaximumSizeToDivergeFromCoreSize = bool(false);

    @Configurable("swarm.hystrix.threadpool.default.metrics.rollingStats.timeInMilliseconds")
    private Defaultable<Long> threadPoolRollingStatsTimeInMilliseconds = longInteger(10000);

    @Configurable("swarm.hystrix.threadpool.default.metrics.rollingPercentile.numBuckets")
    private Defaultable<Integer> threadpoolRollingPercentileNumBuckets = integer(10);
}
