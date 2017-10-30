package org.wildfly.swarm.microprofile.fault.tolerance.hystrix;

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
