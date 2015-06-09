package org.wildfly.swarm.undertow;

import java.io.IOException;

import org.jboss.modules.ModuleLoadException;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.wildfly.swarm.container.Container;
import org.wildfly.swarm.container.WarDeployment;

/**
 * @author Bob McWhirter
 */
public class StaticDeployment extends WarDeployment {

    private final static String JBOSS_DEPLOYMENT_STRUCTURE_CONTENTS =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>  \n" +
                    "<jboss-deployment-structure>  \n" +
                    "    <deployment>  \n" +
                    "         <dependencies>  \n" +
                    "              <module name=\"org.wildfly.swarm.runtime.undertow\"/>  \n" +
                    "        </dependencies>  \n" +
                    "    </deployment>  \n" +
                    "</jboss-deployment-structure>\n";

    private final String base;

    public StaticDeployment(Container container) throws IOException, ModuleLoadException {
        this(container, "/", ".");
    }

    public StaticDeployment(Container container, String contextPath) throws IOException, ModuleLoadException {
        this(container, contextPath, ".");
    }

    public StaticDeployment(Container container, String contextPath, String base) throws IOException, ModuleLoadException {
        super(container, contextPath);
        this.base = base;
    }

    @Override
    public WebArchive getArchive() {
        return getArchive(false);
    }
    @Override
    public WebArchive getArchive(boolean finalize) {
        if ( finalize ) {
            this.archive.addAsWebInfResource(new StringAsset("path-prefix['/'] -> static-content[base='" + this.base + "']"), "undertow-handlers.conf");
            this.archive.addAsWebInfResource(new StringAsset(JBOSS_DEPLOYMENT_STRUCTURE_CONTENTS), "jboss-deployment-structure.xml");
            this.archive.addAsServiceProvider("io.undertow.server.handlers.builder.HandlerBuilder", "org.wildfly.swarm.runtime.undertow.StaticHandlerBuilder");
        }
        return super.getArchive(finalize);
    }

}
