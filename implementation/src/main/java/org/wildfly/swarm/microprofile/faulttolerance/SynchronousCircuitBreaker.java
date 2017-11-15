package org.wildfly.swarm.microprofile.faulttolerance;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.jboss.logging.Logger;
import org.wildfly.swarm.microprofile.faulttolerance.config.CircuitBreakerConfig;

import com.netflix.hystrix.HystrixCircuitBreaker;

/**
 * This is an implementation of the HystrixCircuitBreaker that is expected to be used synchronously by the
 * HystrixCommand implementation to track the state of the circuit. This is needed for the current TCK
 * tests as monitoring circuit state in a background thread does not work with the TCK expectations.
 */
public class SynchronousCircuitBreaker implements HystrixCircuitBreaker {

    private static final Logger LOGGER = Logger.getLogger(SynchronousCircuitBreaker.class);

    enum Status {
        CLOSED, OPEN, HALF_OPEN;
    }

    SynchronousCircuitBreaker(CircuitBreakerConfig config) {
        this.config = config;
        close();
    }

    /**
     * Noop, the hystrix framework calls this more than once so we ignore it
     * @See #incSuccessCount()
     */
    @Override
    public void markSuccess() {
    }

    /**
     * Noop, the hystrix framework calls this more than once so we ignore it
     * @See #incFailureCount()
     */
    @Override
    public void markNonSuccess() {
    }

    @Override
    public boolean isOpen() {
        LOGGER.debugf("isOpen, %s, failures=%d, total=%d", state.get(), failureCount, getTotalCount());
        return state.get().getState() == Status.OPEN;
    }

    /**
     *
     * @return true if the circuit is closed or open and within the delay window
     */
    @Override
    public boolean allowRequest() {
        int execCount = getTotalCount();
        LOGGER.debugf("allowRequest, execCount=%d\n", execCount);
        boolean allowRequest = state.get().allowsExecution(execCount);
        return allowRequest;
    }

    @Override
    public boolean attemptExecution() {
        boolean attemptExecution = state.get().allowsExecution(getTotalCount());
        LOGGER.debugf("attemptExecution(%s), state=%s", attemptExecution, state.get().getState());
        return attemptExecution;
    }

    int incSuccessCount() {
        int count = successCount.incrementAndGet();
        state.get().onSuccess();
        LOGGER.debugf("incSuccessCount(%d), state=%s", count, state.get().getState());
        return count;
    }
    int incFailureCount() {
        int count = failureCount.incrementAndGet();
        state.get().onFailure();
        LOGGER.debugf("incFailureCount(%d), state=%s", count, state.get().getState());
        return count;
    }

    CircuitBreakerConfig getConfig() {
        return config;
    }

    void open() {
        Status prevState = state.get().getState();
        state.set(new OpenState(this));
        LOGGER.debugf("Transition from: %s to: OPEN\n", prevState);
        reset();
    }

    void halfOpen() {
        Status prevState = state.get().getState();
        state.set(new HalfOpenState(this));
        LOGGER.debugf("Transition from: %s to: HALF_OPEN\n", prevState);
        reset();
    }

    void close() {
        Status prevState = state.get() != null ? state.get().getState() : Status.CLOSED;
        state.set(new ClosedState(this));
        LOGGER.debugf("Transition from: %s to: CLOSED\n", prevState);
        reset();
    }

    private int getTotalCount() {
        return successCount.get() + failureCount.get();
    }

    private void reset() {
        successCount.set(0);
        failureCount.set(0);
        LOGGER.debugf("reset(%s)\n", state.get().getState());
    }

    // The circuit state
    private final AtomicReference<State> state = new AtomicReference<>();
    // The circuit configuration
    private final CircuitBreakerConfig config;
    private AtomicInteger successCount = new AtomicInteger(0);
    private AtomicInteger failureCount = new AtomicInteger(0);
}
