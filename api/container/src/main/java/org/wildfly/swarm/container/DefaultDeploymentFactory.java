package org.wildfly.swarm.container;

import org.jboss.modules.ModuleLoadException;

import java.io.IOException;

/**
 * @author Bob McWhirter
 */
public interface DefaultDeploymentFactory {

    int getPriority();
    String getType();
    Deployment create(Container container) throws IOException, ModuleLoadException, Exception;

}
