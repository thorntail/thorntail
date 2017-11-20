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

import org.jboss.logging.Logger;
import org.wildfly.swarm.microprofile.faulttolerance.config.CircuitBreakerConfig;

/**
 *
 */
class HalfOpenState extends RecordingState {

    private static final Logger LOGGER = Logger.getLogger(HalfOpenState.class);

    private final int successThreshold;

    HalfOpenState(SynchronousCircuitBreaker circuit) {
        super(circuit);
        this.successThreshold = circuit.getConfig().get(CircuitBreakerConfig.SUCCESS_THRESHOLD);
    }

    @Override
    boolean allowsExecution(int currentExecutions) {
        boolean allowsExecution = currentExecutions < successThreshold;
        LOGGER.debugf("allowsExecution(%s), currentExecutions=%d\n", allowsExecution, currentExecutions);
        return allowsExecution;
    }

    @Override
    SynchronousCircuitBreaker.Status getState() {
        return SynchronousCircuitBreaker.Status.HALF_OPEN;
    }

    /**
     * Checks to determine if a threshold has been met and the circuit should be opened or closed.
     */
    @Override
    protected void checkThreshold() {
        int requestCount = failureCount.get() + successCount.get();
        if (requestCount == successThreshold) {
            if (successCount.get() >= successThreshold)
                circuit.close();
            else
                circuit.open();
        }
    }

}
