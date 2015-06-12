package org.wildfly.swarm.undertow;

import java.io.IOException;

import org.jboss.modules.ModuleLoadException;
import org.wildfly.swarm.container.Container;
import org.wildfly.swarm.container.WarDeployment;

/**
 * @author Bob McWhirter
 */
public class StaticDeployment extends WarDeployment {

    public StaticDeployment(Container container) throws IOException, ModuleLoadException {
        this(container, "/", ".");
    }

    public StaticDeployment(Container container, String contextPath) throws IOException, ModuleLoadException {
        this(container, contextPath, ".");
    }

    public StaticDeployment(Container container, String contextPath, String base) throws IOException, ModuleLoadException {
        super(container, contextPath);
        staticContent( contextPath, base );
    }

}
