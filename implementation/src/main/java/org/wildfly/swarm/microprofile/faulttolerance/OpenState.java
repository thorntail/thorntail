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

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.jboss.logging.Logger;
import org.wildfly.swarm.microprofile.faulttolerance.config.CircuitBreakerConfig;

/**
 *
 */
class OpenState extends State {

    private static Logger LOGGER = Logger.getLogger(HalfOpenState.class);

    private final long startTime;

    OpenState(SynchronousCircuitBreaker circuit) {
        super(circuit);
        this.startTime = System.currentTimeMillis();
    }

    @Override
    SynchronousCircuitBreaker.Status getState() {
        return SynchronousCircuitBreaker.Status.OPEN;
    }

    @Override
    boolean allowsExecution(int execCount) {
        long delay = circuit.getConfig().get(CircuitBreakerConfig.DELAY);
        ChronoUnit delayUnit = circuit.getConfig().get(CircuitBreakerConfig.DELAY_UNIT);
        Instant start = Instant.ofEpochMilli(startTime);
        Instant now = Instant.now();
        long elapsed = delayUnit.between(start, now);
        boolean allowsExecution = elapsed >= delay;
        LOGGER.debugf("allowsExecution(%s), execCount=%d, elapsed=%d\n", allowsExecution, execCount, elapsed);
        if (allowsExecution) {
            circuit.halfOpen();
        }
        return allowsExecution;
    }

}
