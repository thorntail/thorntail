package org.wildfly.swarm.opentracing;

import org.wildfly.swarm.spi.api.Fraction;
import org.wildfly.swarm.spi.api.annotations.DeploymentModule;

/**
 * @author Juraci Paixão Kröhling
 */
@DeploymentModule(name = "org.wildfly.swarm.opentracing", slot = "deployment")
public class OpenTracingFraction implements Fraction<OpenTracingFraction> {
}
