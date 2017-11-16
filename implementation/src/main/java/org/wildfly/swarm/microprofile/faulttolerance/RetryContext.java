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

import java.time.temporal.ChronoUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.wildfly.swarm.microprofile.faulttolerance.config.RetryConfig;

class RetryContext {

    private final RetryConfig config;

    private final AtomicInteger remainingAttempts;

    private final Long start;

    RetryContext(RetryConfig config) {
        this.config = config;
        start = System.nanoTime();
        remainingAttempts = new AtomicInteger(config.getMaxExecNumber());
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

    public Long getStart() {
        return start;
    }

    public long getMaxDuration() {
        return config.getMaxDuration();
    }

    public long getDelay() {
        return config.getDelay();
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
