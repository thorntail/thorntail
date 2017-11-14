package org.wildfly.swarm.microprofile.faulttolerance;

import java.util.concurrent.atomic.AtomicInteger;

import org.jboss.logging.Logger;
import org.wildfly.swarm.microprofile.faulttolerance.config.CircuitBreakerConfig;

public class HalfOpenState implements State {
    private static Logger log = Logger.getLogger(HalfOpenState.class);
    private final SynchronousCircuitBreaker circuit;
    private AtomicInteger successCount = new AtomicInteger(0);
    private AtomicInteger failureCount = new AtomicInteger(0);
    private int successThreshold;
    private double failureRatio;

    public HalfOpenState(SynchronousCircuitBreaker circuit) {
        this.circuit = circuit;
        failureRatio = circuit.getConfig().get(CircuitBreakerConfig.FAILURE_RATIO, Double.class);
        successThreshold = circuit.getConfig().get(CircuitBreakerConfig.SUCCESS_THRESHOLD, Integer.class);
    }

    @Override
    public boolean allowsExecution(int currentExecutions) {
        boolean allowsExecution = currentExecutions < successThreshold;
        log.debugf("allowsExecution(%s), currentExecutions=%d\n", allowsExecution, currentExecutions);
        return allowsExecution;
    }

    @Override
    public SynchronousCircuitBreaker.Status getState() {
        return SynchronousCircuitBreaker.Status.HALF_OPEN;
    }

    @Override
    public synchronized void recordFailure() {
        failureCount.incrementAndGet();
        checkThreshold();
    }

    @Override
    public synchronized void recordSuccess() {
        successCount.incrementAndGet();
        checkThreshold();
    }

    /**
     * Checks to determine if a threshold has been met and the circuit should be opened or closed.
     *
     */
    synchronized void checkThreshold() {

        int requestCount = failureCount.get() + successCount.get();
        double failureCheck = failureCount.get() / requestCount;
        if (successThreshold > 0) {
            if (requestCount == successThreshold)
                if (successCount.get() >= successThreshold)
                    circuit.close();
                else
                    circuit.open();
        } else if (failureRatio > 0) {
            if (requestCount == successThreshold)
                if (failureCheck >= failureRatio)
                    circuit.open();
                else
                    circuit.close();
        } else {
            if (successCount.get() == 1)
                circuit.close();
            else
                circuit.open();
        }
    }

}
