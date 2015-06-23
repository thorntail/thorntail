package org.wildfly.swarm.jaxrs;

import org.wildfly.swarm.container.Container;
import org.wildfly.swarm.container.DefaultDeploymentFactory;
import org.wildfly.swarm.container.Deployment;

/**
 * @author Bob McWhirter
 */
public class DefaultJAXRSWarDeploymentFactory implements DefaultDeploymentFactory {

    @Override
    public int getPriority() {
        return 1000;
    }

    @Override
    public String getType() {
        return "war";
    }

    @Override
    public Deployment create(Container container) throws Exception {
        return new DefaultJAXRSWarDeployment(container);
    }
}
