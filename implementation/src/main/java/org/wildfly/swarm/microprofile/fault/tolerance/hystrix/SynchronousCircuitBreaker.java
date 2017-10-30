package org.wildfly.swarm.microprofile.fault.tolerance.hystrix;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import com.netflix.hystrix.HystrixCircuitBreaker;
import com.netflix.hystrix.HystrixCommandKey;
import org.jboss.logging.Logger;
import org.wildfly.swarm.microprofile.fault.tolerance.hystrix.config.CircuitBreakerConfig;

/**
 * This is an implementation of the HystrixCircuitBreaker that is expected to be used synchronously by the
 * HystrixCommand implementation to track the state of the circuit. This is needed for the current TCK
 * tests as monitoring circuit state in a background thread does not work with the TCK expectations.
 */
public class SynchronousCircuitBreaker implements HystrixCircuitBreaker {
    private static Logger log = Logger.getLogger(SynchronousCircuitBreaker.class);
    enum Status {
        CLOSED, OPEN, HALF_OPEN;
    }

    static SynchronousCircuitBreaker getCircuitBreaker(HystrixCommandKey key, final CircuitBreakerConfig config) {
        Function<HystrixCommandKey, SynchronousCircuitBreaker> newFunc = (key1) -> new SynchronousCircuitBreaker(config);
        SynchronousCircuitBreaker circuitBreaker = circuitBreakerMap.computeIfAbsent(key, newFunc);
        log.debugf("getCircuitBreaker, key=%s\n", key.name());
        return circuitBreaker;
    }

    SynchronousCircuitBreaker(CircuitBreakerConfig config) {
        this.config = config;
        close();
    }

    /**
     * Noop, the hysterix framework calls this more than once so we ignore it
     * @See #incSuccessCount()
     */
    @Override
    public void markSuccess() {
    }

    /**
     * Noop, the hysterix framework calls this more than once so we ignore it
     * @See #incFailureCount()
     */
    @Override
    public void markNonSuccess() {
    }

    @Override
    public boolean isOpen() {
        log.debugf("isOpen, %s, failures=%d, total=%d", state.get(), failureCount, getTotalCount());

        return state.get().getState() == Status.OPEN;
    }

    /**
     *
     * @return true if the circuit is closed or open and within the delay window
     */
    @Override
    public boolean allowRequest() {
        int execCount = getTotalCount();
        log.debugf("allowRequest, execCount=%d\n", execCount);
        boolean allowRequest = state.get().allowsExecution(execCount);
        return allowRequest;
    }

    @Override
    public boolean attemptExecution() {
        boolean attemptExecution = state.get().allowsExecution(getTotalCount());
        log.debugf("attemptExecution(%s), state=%s", attemptExecution, state.get().getState());
        return attemptExecution;
    }

    int incSuccessCount() {
        int count = successCount.incrementAndGet();
        state.get().recordSuccess();
        log.debugf("incSuccessCount(%d), state=%s", count, state.get().getState());
        return count;
    }
    int incFailureCount() {
        int count = failureCount.incrementAndGet();
        state.get().recordFailure();
        log.debugf("incFailureCount(%d), state=%s", count, state.get().getState());
        return count;
    }
    CircuitBreakerConfig getConfig() {
        return config;
    }

    void open() {
        Status prevState = state.get().getState();
        state.set(new OpenState(this));
        log.debugf("Transition from: %s to: OPEN\n", prevState);
        reset();
    }
    void halfOpen() {
        Status prevState = state.get().getState();
        state.set(new HalfOpenState(this));
        log.debugf("Transition from: %s to: HALF_OPEN\n", prevState);
        reset();
    }
    void close() {
        Status prevState = state.get() != null ? state.get().getState() : Status.CLOSED;
        state.set(new ClosedState(this));
        log.debugf("Transition from: %s to: CLOSED\n", prevState);
        reset();
    }

    private int getTotalCount() {
        return successCount.get() + failureCount.get();
    }
    private void reset() {
        circuitOpenedTime.set(-1);
        successCount.set(0);
        failureCount.set(0);
        log.debugf("reset(%s)\n", state.get().getState());
    }

    // The circuit state
    private final AtomicReference<State> state = new AtomicReference<>();
    // The last time the circuit was opened, -1 for closed
    private final AtomicLong circuitOpenedTime = new AtomicLong(-1);
    // The circuit configuration
    private final CircuitBreakerConfig config;
    private AtomicInteger successCount = new AtomicInteger(0);
    private AtomicInteger failureCount = new AtomicInteger(0);
    private static ConcurrentHashMap<HystrixCommandKey, SynchronousCircuitBreaker> circuitBreakerMap = new ConcurrentHashMap<>();
}
