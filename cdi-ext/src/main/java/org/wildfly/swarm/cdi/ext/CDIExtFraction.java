package org.wildfly.swarm.cdi.ext;

import org.wildfly.swarm.spi.api.Fraction;
import org.wildfly.swarm.spi.api.annotations.DeploymentModule;

/**
 * @author Bob McWhirter
 */
@DeploymentModule(name = "org.wildfly.swarm.cdi.ext", slot = "deployment", export = true, metaInf = DeploymentModule.MetaInfDisposition.IMPORT)
public class CDIExtFraction implements Fraction<CDIExtFraction> {
}
