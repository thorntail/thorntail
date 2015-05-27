package org.wildfly.swarm.datasources;

import org.wildfly.swarm.container.Container;
import org.wildfly.swarm.container.DependencyDeployment;

import java.io.IOException;

/**
 * @author Bob McWhirter
 */
public class DriverDeployment extends DependencyDeployment {

    public DriverDeployment(Container container, String gav, String name) throws Exception {
        super(container, gav, name);
    }
}
