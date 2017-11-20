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

import org.wildfly.swarm.microprofile.faulttolerance.config.CircuitBreakerConfig;

/**
 * The circuit closed state logic
 */
class ClosedState extends RecordingState {

    private final double failureRatio;

    ClosedState(SynchronousCircuitBreaker circuit) {
        super(circuit);
        this.failureRatio = circuit.getConfig().get(CircuitBreakerConfig.FAILURE_RATIO);
    }

    @Override
    boolean allowsExecution(int executionCount) {
        return true;
    }

    @Override
    SynchronousCircuitBreaker.Status getState() {
        return SynchronousCircuitBreaker.Status.CLOSED;
    }

    @Override
    protected void checkThreshold() {
        int requestCount = getRequestCount();
        double failureCheck = failureCount.get() / requestCount;
        int requestVolumeThreshold = circuit.getConfig().get(CircuitBreakerConfig.REQUEST_VOLUME_THRESHOLD);
        if (requestCount >= requestVolumeThreshold && failureCheck >= failureRatio) {
            circuit.open();
        }
        if (failureRatio <= 0 && failureCheck == 1) {
            circuit.open();
        }
    }

}
