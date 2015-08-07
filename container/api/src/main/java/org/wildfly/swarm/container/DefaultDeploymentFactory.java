package org.wildfly.swarm.container;

import org.jboss.modules.ModuleLoadException;
import org.jboss.shrinkwrap.api.Archive;

import java.io.IOException;

/**
 * @author Bob McWhirter
 */
public interface DefaultDeploymentFactory {

    int getPriority();
    String getType();
    Archive create(Container container) throws IOException, ModuleLoadException, Exception;

}
