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

import static org.wildfly.swarm.microprofile.faulttolerance.SynchronousCircuitBreaker.Status.CLOSED;
import static org.wildfly.swarm.microprofile.faulttolerance.SynchronousCircuitBreaker.Status.HALF_OPEN;
import static org.wildfly.swarm.microprofile.faulttolerance.SynchronousCircuitBreaker.Status.OPEN;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import org.jboss.logging.Logger;
import org.wildfly.swarm.microprofile.faulttolerance.config.CircuitBreakerConfig;

import com.netflix.hystrix.HystrixCircuitBreaker;

/**
 * This is an implementation of the HystrixCircuitBreaker that is expected to be used synchronously by the HystrixCommand implementation to track the state of
 * the circuit. This is needed for the current TCK tests as monitoring circuit state in a background thread does not work with the TCK expectations.
 *
 * @see HystrixCommandInterceptor#SYNC_CIRCUIT_BREAKER_KEY
 */
class SynchronousCircuitBreaker implements HystrixCircuitBreaker {

    private static final Logger LOGGER = Logger.getLogger(SynchronousCircuitBreaker.class);

    enum Status {
        CLOSED, OPEN, HALF_OPEN;
    }

    SynchronousCircuitBreaker(CircuitBreakerConfig config) {
        this.config = config;
        this.status = new AtomicReference<>(CLOSED);
        this.circuitOpenedAt = new AtomicLong(-1);
        this.successCount = new AtomicInteger(0);
        this.failureCount = new AtomicInteger(0);
        this.halfOpenAttempts = new AtomicInteger(0);
        this.id = config.getMethodInfo();
    }

    @Override
    public void markSuccess() {
        // No-op, used by Hystrix to handle half-open state
    }

    @Override
    public void markNonSuccess() {
        // No-op, used by Hystrix to handle half-open state
    }

    @Override
    public synchronized boolean isOpen() {
        return circuitOpenedAt.get() >= 0;
    }

    @Override
    public synchronized boolean allowRequest() {
        // Allow next request if:
        // 1. circuit is CLOSED
        // 2. circuit is OPEN and specified delay passed
        // 3. circuit is HALF_OPEN and next attempt is allowed
        switch (status.get()) {
            case CLOSED:
                return true;
            case HALF_OPEN:
                return isHalfOpenAttemptAllowed();
            case OPEN:
                return isAfterDelay();
            default:
                return false;
        }
    }

    @Override
    public synchronized boolean attemptExecution() {
        // Allow next execution if:
        // 1. circuit is CLOSED
        // 2. circuit is HALF_OPEN and next attempt is allowed
        // 3. circuit is OPEN and specified delay passed - transition to HALF_OPEN
        switch (status.get()) {
            case CLOSED:
                return true;
            case HALF_OPEN:
                if (isHalfOpenAttemptAllowed()) {
                    halfOpenAttempts.incrementAndGet();
                    return true;
                }
                return false;
            case OPEN:
                if (isAfterDelay()) {
                    LOGGER.debugf("OPEN >> HALF_OPEN [id:%s]", id);
                    status.set(HALF_OPEN);
                    halfOpenAttempts.set(1);
                    resetCounts();
                    return true;
                }
                return false;
            default:
                return false;
        }
    }

    synchronized void executionSucceeded() {
        successCount.incrementAndGet();
        // Transition to CLOSED if HALF_OPEN and successThreshold reached
        if (HALF_OPEN == status.get() && isSuccessThresholdReached()) {
            LOGGER.debugf("HALF_OPEN >> CLOSED [id:%s]", id);
            status.set(CLOSED);
            circuitOpenedAt.set(-1);
            resetCounts();
        }
    }

    synchronized void executionFailed() {
        failureCount.incrementAndGet();
        // Transition to OPEN if HALF_OPEN
        // Transition to OPEN if CLOSED and failure threshold reached
        Status current = status.get();
        if (HALF_OPEN == current || (CLOSED == current && isFailureThresholdReached())) {
            LOGGER.debugf("%s >> OPEN [id:%s]", current, id);
            status.set(OPEN);
            circuitOpenedAt.set(System.currentTimeMillis());
            resetCounts();
        }
    }

    private boolean isAfterDelay() {
        long openedAt = circuitOpenedAt.get();
        long delay = config.get(CircuitBreakerConfig.DELAY);
        if (delay == 0) {
            return true;
        }
        ChronoUnit delayUnit = config.get(CircuitBreakerConfig.DELAY_UNIT);
        long elapsed;
        if (delayUnit.equals(ChronoUnit.MILLIS)) {
            elapsed = System.currentTimeMillis() - openedAt;
        } else {
            Instant start = Instant.ofEpochMilli(openedAt);
            Instant now = Instant.now();
            elapsed = delayUnit.between(start, now);
        }
        return elapsed >= delay;
    }

    private boolean isFailureThresholdReached() {
        int requestCount = getRequestCount();
        int requestVolumeThreshold = config.get(CircuitBreakerConfig.REQUEST_VOLUME_THRESHOLD);
        if (requestCount < requestVolumeThreshold) {
            return false;
        }
        double failureCheck = failureCount.get() / requestCount;
        double failureRatio = config.get(CircuitBreakerConfig.FAILURE_RATIO);
        return (failureCheck >= failureRatio) || (failureRatio <= 0 && failureCheck == 1);
    }

    private boolean isSuccessThresholdReached() {
        return successCount.get() >= config.get(CircuitBreakerConfig.SUCCESS_THRESHOLD, Integer.class);
    }

    private boolean isHalfOpenAttemptAllowed() {
        return halfOpenAttempts.get() < config.get(CircuitBreakerConfig.SUCCESS_THRESHOLD, Integer.class);
    }

    private int getRequestCount() {
        return successCount.get() + failureCount.get();
    }

    private void resetCounts() {
        successCount.set(0);
        failureCount.set(0);
    }

    private final AtomicReference<Status> status;

    private final AtomicLong circuitOpenedAt;

    private final CircuitBreakerConfig config;

    private final AtomicInteger successCount;

    private final AtomicInteger failureCount;

    private final AtomicInteger halfOpenAttempts;

    private final String id;

}