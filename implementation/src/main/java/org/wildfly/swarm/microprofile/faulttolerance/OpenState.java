package org.wildfly.swarm.microprofile.faulttolerance;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.jboss.logging.Logger;
import org.wildfly.swarm.microprofile.faulttolerance.config.CircuitBreakerConfig;

/**
 *
 */
public class OpenState implements State {
    private static Logger log = Logger.getLogger(HalfOpenState.class);
    private final SynchronousCircuitBreaker circuit;
    private final long startTime = System.currentTimeMillis();

    public OpenState(SynchronousCircuitBreaker circuit) {
        this.circuit = circuit;
    }
    @Override
    public SynchronousCircuitBreaker.Status getState() {
        return SynchronousCircuitBreaker.Status.OPEN;
    }

    @Override
    public boolean allowsExecution(int execCount) {
        long delay = circuit.getConfig().get(CircuitBreakerConfig.DELAY, Long.class);
        ChronoUnit delayUnit = circuit.getConfig().get(CircuitBreakerConfig.DELAY_UNIT, ChronoUnit.class);
        Instant start = Instant.ofEpochMilli(startTime);
        Instant now = Instant.now();
        long elapsed = delayUnit.between(start, now);
        boolean allowsExecution = elapsed >= delay;
        log.debugf("allowsExecution(%s), execCount=%d, elapsed=%d\n", allowsExecution, execCount, elapsed);
        if (allowsExecution) {
            circuit.halfOpen();
        }

        return allowsExecution;
    }

}
