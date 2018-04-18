package io.thorntail.faulttolerance.impl;

import org.eclipse.microprofile.faulttolerance.CircuitBreaker;

/**
 * @author Ken Finnigan
 */
public interface FaultToleranceConstants {

    /**
     * This config property key can be used to disable synchronous circuit breaker functionality. If disabled, {@link CircuitBreaker#successThreshold()} of
     * value greater than 1 is not supported.
     * <p>
     * Moreover, circuit breaker does not necessarily transition from CLOSED to OPEN immediately when a fault tolerance operation completes. See also
     * <a href="https://github.com/Netflix/Hystrix/wiki/Configuration#metrics.healthSnapshot.intervalInMilliseconds">Hystrix configuration</a>
     * </p>
     * <p>
     * In general, application developers are encouraged to disable this feature on high-volume circuits and in production environments.
     * </p>
     */
    String SYNC_CIRCUIT_BREAKER_KEY = "org_wildfly_swarm_microprofile_faulttolerance_syncCircuitBreaker";
}
