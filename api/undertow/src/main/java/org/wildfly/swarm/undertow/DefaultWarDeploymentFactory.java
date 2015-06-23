package org.wildfly.swarm.undertow;

import org.wildfly.swarm.container.Container;
import org.wildfly.swarm.container.DefaultDeploymentFactory;
import org.wildfly.swarm.container.Deployment;

/**
 * @author Bob McWhirter
 */
public class DefaultWarDeploymentFactory implements DefaultDeploymentFactory {

    @Override
    public int getPriority() {
        return 0;
    }

    @Override
    public String getType() {
        return "war";
    }

    @Override
    public Deployment create(Container container) throws Exception {
        return new DefaultWarDeployment(container);
    }
}
