package org.wildfly.swarm.microprofile.faulttolerance;

/**
 *
 */
public interface State {
    boolean allowsExecution(int execCount);

    SynchronousCircuitBreaker.Status getState();

    default void recordFailure() {
    }

    default void recordSuccess() {
    }
}
