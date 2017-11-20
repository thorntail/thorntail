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
import java.time.temporal.ChronoUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.wildfly.swarm.microprofile.faulttolerance.config.RetryConfig;

class RetryContext {

    private final RetryConfig config;

    private final AtomicInteger remainingAttempts;

    private final long start;

    private final long maxDuration;

    private final long delay;

    RetryContext(RetryConfig config) {
        this.config = config;
        this.start = System.nanoTime();
        this.remainingAttempts = new AtomicInteger(config.<Integer>get(RetryConfig.MAX_RETRIES) + 1);
        this.maxDuration = Duration.of(config.get(RetryConfig.MAX_DURATION), config.get(RetryConfig.DURATION_UNIT)).toNanos();
        this.delay = Duration.of(config.get(RetryConfig.DELAY), config.get(RetryConfig.DELAY_UNIT)).toMillis();
    }

    public RetryConfig getConfig() {
        return config;
    }

    public void doRetry() {
        remainingAttempts.decrementAndGet();
    }

    public boolean shouldRetry() {
        return remainingAttempts.get() > 0;
    }

    public long getStart() {
        return start;
    }

    public long getMaxDuration() {
        return maxDuration;
    }

    public long getDelay() {
        return delay;
    }

    public Class<?>[] getAbortOn() {
        return config.getAbortOn();
    }

    public Class<?>[] getRetryOn() {
        return config.getRetryOn();
    }

    public Long getJitter() {
        return config.getJitter();
    }

    public ChronoUnit getJitterDelayUnit() {
        return config.getJitterDelayUnit();
    }

    @Override
    public String toString() {
        return "RetryContext [remainingAttempts=" + remainingAttempts + ", start=" + start + "]";
    }

}
