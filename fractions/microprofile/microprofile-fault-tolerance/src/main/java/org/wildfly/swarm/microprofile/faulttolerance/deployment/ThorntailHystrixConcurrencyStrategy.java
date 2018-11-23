package org.wildfly.swarm.microprofile.faulttolerance.deployment;

import io.smallrye.faulttolerance.DefaultHystrixConcurrencyStrategy;

import javax.annotation.Priority;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Alternative;

/**
 * Workaround for https://issues.jboss.org/browse/WFLY-11373
 */
@Priority(1000)
@Alternative
@ApplicationScoped
public class ThorntailHystrixConcurrencyStrategy extends DefaultHystrixConcurrencyStrategy {
}
