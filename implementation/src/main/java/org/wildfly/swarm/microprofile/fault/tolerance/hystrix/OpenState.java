package org.wildfly.swarm.microprofile.fault.tolerance.hystrix;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.jboss.logging.Logger;

import static org.wildfly.swarm.microprofile.fault.tolerance.hystrix.config.CircuitBreakerConfig.DELAY;
import static org.wildfly.swarm.microprofile.fault.tolerance.hystrix.config.CircuitBreakerConfig.DELAY_UNIT;

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
        long delay = circuit.getConfig().get(DELAY, Long.class);
        ChronoUnit delayUnit = circuit.getConfig().get(DELAY_UNIT, ChronoUnit.class);
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
