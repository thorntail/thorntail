package org.wildfly.swarm.monitor;

/**
 * @author Heiko Braun
 * @since 23/03/16
 */
public interface HealthCheck {

    HealthStatus perform();
}
