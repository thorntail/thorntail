package org.wildfly.swarm.microprofile.fault.tolerance.hystrix;

import java.util.concurrent.atomic.AtomicInteger;

import org.wildfly.swarm.microprofile.fault.tolerance.hystrix.config.CircuitBreakerConfig;

/**
 * The circuit closed state logic
 */
public class ClosedState implements State {
    private final SynchronousCircuitBreaker circuit;
    private AtomicInteger successCount = new AtomicInteger(0);
    private AtomicInteger failureCount = new AtomicInteger(0);
    private double failureRatio;


    public ClosedState(SynchronousCircuitBreaker circuit) {
        this.circuit = circuit;
        this.failureRatio = circuit.getConfig().get(CircuitBreakerConfig.FAILURE_RATIO, Double.class);
    }

    @Override
    public boolean allowsExecution(int executionCount) {
        return true;
    }

    @Override
    public SynchronousCircuitBreaker.Status getState() {
        return SynchronousCircuitBreaker.Status.CLOSED;
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

    synchronized void checkThreshold() {
        int requestCount = failureCount.get() + successCount.get();
        double failureCheck = failureCount.get() / requestCount;
        int requestVolumeThreshold = circuit.getConfig().get(CircuitBreakerConfig.REQUEST_VOLUME_THRESHOLD, Integer.class);
        if (requestCount >= requestVolumeThreshold && failureCheck >= failureRatio) {
            circuit.open();
        }

        if (failureRatio <= 0 && failureCheck == 1) {
            circuit.open();
        }
    }

}
