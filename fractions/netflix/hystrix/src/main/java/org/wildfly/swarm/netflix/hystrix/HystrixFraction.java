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

import static org.wildfly.swarm.netflix.hystrix.HystrixProperties.DEFAULT_STREAM_PATH;
import static org.wildfly.swarm.spi.api.Defaultable.bool;
import static org.wildfly.swarm.spi.api.Defaultable.integer;
import static org.wildfly.swarm.spi.api.Defaultable.longInteger;
import static org.wildfly.swarm.spi.api.Defaultable.string;

import org.wildfly.swarm.config.runtime.AttributeDocumentation;
import org.wildfly.swarm.spi.api.Defaultable;
import org.wildfly.swarm.spi.api.Fraction;
import org.wildfly.swarm.spi.api.annotations.Configurable;
import org.wildfly.swarm.spi.api.annotations.DeploymentModule;
import org.wildfly.swarm.spi.api.annotations.DeploymentModules;

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

    @AttributeDocumentation("Context path for the stream")
    @Configurable("thorntail.hystrix.stream.path")
    private Defaultable<String> streamPath = string(DEFAULT_STREAM_PATH);

    @AttributeDocumentation("Isolation strategy (THREAD or SEMAPHORE)")
    @Configurable("thorntail.hystrix.command.default.execution.isolation.strategy")
    private Defaultable<String> isolationStrategy = string("THREAD");

    @AttributeDocumentation("The time in milliseconds after which the caller will observe a timeout and walk away from the command execution")
    @Configurable("thorntail.hystrix.command.default.execution.isolation.thread.timeoutInMilliseconds")
    private Defaultable<Long> threadTimeoutInMilliseconds = longInteger(1000);

    @AttributeDocumentation("Indicates whether the HystrixCommand.run() execution should have a timeout")
    @Configurable("thorntail.hystrix.command.default.execution.timeout.enabled")
    private Defaultable<Boolean> threadTimeoutEnabled = bool(true);

    @AttributeDocumentation("Indicates whether the HystrixCommand.run() execution should be interrupted when a timeout occurs")
    @Configurable("thorntail.hystrix.command.default.execution.isolation.thread.interruptOnTimeout")
    private Defaultable<Boolean> threadInterruptOnTimeout = bool(true);

    @AttributeDocumentation("Indicates whether the HystrixCommand.run() execution should be interrupted when a cancellation occurs")
    @Configurable("thorntail.hystrix.command.default.execution.isolation.thread.interruptOnCancel")
    private Defaultable<Boolean> threadInterruptOnCancel = bool(false);

    @AttributeDocumentation("The maximum number of requests allowed to a HystrixCommand.run() method when you are using ExecutionIsolationStrategy.SEMAPHORE")
    @Configurable("thorntail.hystrix.command.default.execution.isolation.semaphore.maxConcurrentRequests")
    private Defaultable<Integer> semaphorMaxConcurrentRequests = integer(10);

    @AttributeDocumentation("The maximum number of requests allowed to a HystrixCommand.getFallback() method when you are using ExecutionIsolationStrategy.SEMAPHORE")
    @Configurable("thorntail.hystrix.command.default.fallback.isolation.semaphore.maxConcurrentRequests")
    private Defaultable<Integer> fallbackSemaphoreMaxConcurrentRequests = integer(10);

    @AttributeDocumentation("Determines whether a call to HystrixCommand.getFallback() will be attempted when failure or rejection occurs")
    @Configurable("thorntail.hystrix.command.default.fallback.enabled")
    private Defaultable<Boolean> fallbackEnabled = bool(true);

    @AttributeDocumentation("Determines whether a circuit breaker will be used to track health and to short-circuit requests if it trips")
    @Configurable("thorntail.hystrix.command.default.circuitBreaker.enabled")
    private Defaultable<Boolean> circuitBreakerEnabled = bool(true);

    @AttributeDocumentation("The minimum number of requests in a rolling window that will trip the circuit")
    @Configurable("thorntail.hystrix.command.default.circuitBreaker.requestVolumeThreshold")
    private Defaultable<Integer> circuitBreakerRequestVolumeThreshold = integer(20);

    @AttributeDocumentation("The amount of time, after tripping the circuit, to reject requests before allowing attempts again to determine if the circuit should again be closed")
    @Configurable("thorntail.hystrix.command.default.circuitBreaker.sleepWindowInMilliseconds")
    private Defaultable<Long> circuitBreakerSleepWindowInMilliseconds = longInteger(5000);

    @AttributeDocumentation("The error percentage at or above which the circuit should trip open and start short-circuiting requests to fallback logic")
    @Configurable("thorntail.hystrix.command.default.circuitBreaker.errorThresholdPercentage")
    private Defaultable<Integer> circuitBreakererrorThresholdPercentage = integer(50);

    @AttributeDocumentation("If true, forces the circuit breaker into an open (tripped) state in which it will reject all requests")
    @Configurable("thorntail.hystrix.command.default.circuitBreaker.forceOpen")
    private Defaultable<Boolean> circuitBreakerForceOpen = bool(false);

    @AttributeDocumentation("If true, forces the circuit breaker into a closed state in which it will allow requests regardless of the error percentage")
    @Configurable("thorntail.hystrix.command.default.circuitBreaker.forceClosed")
    private Defaultable<Boolean> circuitBreakerForceClosed = bool(false);

    @AttributeDocumentation("The duration of the statistical rolling window, in milliseconds. This is how long Hystrix keeps metrics for the circuit breaker to use and for publishing")
    @Configurable("thorntail.hystrix.command.default.metrics.rollingStats.timeInMilliseconds")
    private Defaultable<Long> rollingStatsTimeInMilliseconds = longInteger(10000);

    @AttributeDocumentation("The number of buckets the rolling statistical window is divided into")
    @Configurable("thorntail.hystrix.command.default.metrics.rollingStats.numBuckets")
    private Defaultable<Integer> rollingStatsNumBuckets = integer(10);

    @AttributeDocumentation("Indicates whether execution latencies should be tracked and calculated as percentiles")
    @Configurable("thorntail.hystrix.command.default.metrics.rollingPercentile.enabled")
    private Defaultable<Boolean> rollingPercentileEnabled = bool(true);

    @AttributeDocumentation("The duration of the rolling window in which execution times are kept to allow for percentile calculations, in milliseconds")
    @Configurable("thorntail.hystrix.command.default.metrics.rollingPercentile.timeInMilliseconds")
    private Defaultable<Long> rollingPercentileTimeInMilliseconds = longInteger(60000);

    @AttributeDocumentation("The number of buckets the rollingPercentile window will be divided into")
    @Configurable("thorntail.hystrix.command.default.metrics.rollingPercentile.numBuckets")
    private Defaultable<Integer> rollingPercentileNumBuckets = integer(6);

    @AttributeDocumentation("The maximum number of execution times that are kept per bucket")
    @Configurable("thorntail.hystrix.command.default.metrics.rollingPercentile.bucketSize")
    private Defaultable<Integer> rollingPercentileBucketSize = integer(100);

    @AttributeDocumentation("The time to wait, in milliseconds, between allowing health snapshots to be taken that calculate success and error percentages and affect circuit breaker status")
    @Configurable("thorntail.hystrix.command.default.metrics.healthSnapshot.intervalInMilliseconds")
    private Defaultable<Long> healthSnapshotIntervalInMilliseconds = longInteger(500);

    @AttributeDocumentation("Indicates whether HystrixCommand.getCacheKey() should be used with HystrixRequestCache to provide de-duplication functionality via request-scoped caching")
    @Configurable("thorntail.hystrix.command.default.requestCache.enabled")
    private Defaultable<Boolean> requestCacheEnabled = bool(true);

    @AttributeDocumentation("Indicates whether HystrixCommand execution and events should be logged to HystrixRequestLog")
    @Configurable("thorntail.hystrix.command.default.requestLog.enabled")
    private Defaultable<Boolean> requestLogEnabled = bool(true);

    @AttributeDocumentation("The maximum number of requests allowed in a batch before this triggers a batch execution")
    @Configurable("thorntail.hystrix.collapser.default.maxRequestsInBatch")
    private Defaultable<Integer> maxRequestsInBatch = integer(Integer.MAX_VALUE);

    @AttributeDocumentation("The number of milliseconds after the creation of the batch that its execution is triggered")
    @Configurable("thorntail.hystrix.collapser.default.timerDelayInMilliseconds")
    private Defaultable<Long> timerDelayInMilliseconds = longInteger(10);

    @AttributeDocumentation("Indicates whether request caching is enabled for HystrixCollapser.execute() and HystrixCollapser.queue() invocations")
    @Configurable("thorntail.hystrix.collapser.default.requestCache.enabled")
    private Defaultable<Boolean> collapserRequestCacheEnabled = bool(true);

    @AttributeDocumentation("The core thread-pool size")
    @Configurable("thorntail.hystrix.threadpool.default.coreSize")
    private Defaultable<Integer> threadpoolCoreSize = integer(10);

    @AttributeDocumentation("The maximum thread-pool size")
    @Configurable("thorntail.hystrix.threadpool.default.maximumSize")
    private Defaultable<Integer> threadpoolMaximumSize = integer(10);

    @AttributeDocumentation("The maximum queue size of the BlockingQueue implementation")
    @Configurable("thorntail.hystrix.threadpool.default.maxQueueSize")
    private Defaultable<Integer> threadpoolMaxQueueSize = integer(-1);

    @AttributeDocumentation("The queue size rejection threshold - an artificial maximum queue size at which rejections will occur even if maxQueueSize has not been reached")
    @Configurable("thorntail.hystrix.threadpool.default.queueSizeRejectionThreshold")
    private Defaultable<Integer> threadpoolQueueSizeRejectionThreshold = integer(5);

    @AttributeDocumentation("The keep-alive time, in minutes")
    @Configurable("thorntail.hystrix.threadpool.default.keepAliveTimeMinutes")
    private Defaultable<Integer> threadpoolKeepAliveTimeMinutes = integer(1);

    @AttributeDocumentation("Allows the configuration for maximumSize to take effect")
    @Configurable("thorntail.hystrix.threadpool.default.allowMaximumSizeToDivergeFromCoreSize")
    private Defaultable<Boolean> threadpoolKeepAllowMaximumSizeToDivergeFromCoreSize = bool(false);

    @AttributeDocumentation("The duration of the statistical rolling window, in milliseconds")
    @Configurable("thorntail.hystrix.threadpool.default.metrics.rollingStats.timeInMilliseconds")
    private Defaultable<Long> threadPoolRollingStatsTimeInMilliseconds = longInteger(10000);

    @AttributeDocumentation("The number of buckets the rolling statistical window is divided into")
    @Configurable("thorntail.hystrix.threadpool.default.metrics.rollingPercentile.numBuckets")
    private Defaultable<Integer> threadpoolRollingPercentileNumBuckets = integer(10);
}
