package org.wildfly.swarm.jpa.spatial;

import org.wildfly.swarm.spi.api.Fraction;
import org.wildfly.swarm.spi.api.annotations.DeploymentModule;

/**
 * @author Bob McWhirter
 */
@DeploymentModule(name = "org.hibernate.spatial")
public class JPASpatialFraction implements Fraction<JPASpatialFraction> {
}
