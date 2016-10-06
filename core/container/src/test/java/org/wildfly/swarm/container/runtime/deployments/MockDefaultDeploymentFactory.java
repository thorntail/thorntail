package org.wildfly.swarm.container.runtime.deployments;

import org.jboss.shrinkwrap.api.Archive;
import org.wildfly.swarm.spi.api.DefaultDeploymentFactory;

/**
 * @author Bob McWhirter
 */
public class MockDefaultDeploymentFactory extends DefaultDeploymentFactory {
    private final String type;

    private final int prio;

    public MockDefaultDeploymentFactory(String type, int prio) {
        this.type = type;
        this.prio = prio;
    }
    @Override
    public int getPriority() {
        return this.prio;
    }

    @Override
    public String getType() {
        return this.type;
    }

    @Override
    public Archive create() throws Exception {
        return null;
    }

    @Override
    protected boolean setupUsingMaven(Archive<?> archive) throws Exception {
        return false;
    }
}
