package org.wildfly.swarm.datasources;

import org.wildfly.swarm.container.DependencyDeployment;

import java.io.IOException;

/**
 * @author Bob McWhirter
 */
public class DriverDeployment extends DependencyDeployment {

    public DriverDeployment(String gav, String name) throws IOException {
        super(gav, name);
    }
}
