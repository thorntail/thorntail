package org.wildfly.swarm.cdi.config;

import org.wildfly.swarm.spi.api.Fraction;
import org.wildfly.swarm.spi.api.annotations.DeploymentModule;

/**
 * @author Bob McWhirter
 */
@DeploymentModule(name = "org.wildfly.swarm.cdi.config", slot = "deployment", export = true, metaInf = DeploymentModule.MetaInfDisposition.IMPORT)
public class CDIConfigFraction implements Fraction<CDIConfigFraction> {
}
