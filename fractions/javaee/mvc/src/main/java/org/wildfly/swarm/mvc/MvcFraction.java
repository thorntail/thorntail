package org.wildfly.swarm.mvc;

import org.wildfly.swarm.spi.api.Fraction;
import org.wildfly.swarm.spi.api.Module;
import org.wildfly.swarm.spi.api.annotations.DeploymentModule;

@DeploymentModule(name = "org.eclipse.krazo", services = Module.ServiceHandling.IMPORT)
public class MvcFraction implements Fraction<MvcFraction> {
}
