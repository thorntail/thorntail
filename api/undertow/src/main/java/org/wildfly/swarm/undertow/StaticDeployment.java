package org.wildfly.swarm.undertow;

import org.jboss.modules.ModuleLoadException;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.asset.FileAsset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.wildfly.swarm.container.Container;
import org.wildfly.swarm.container.Deployment;
import org.wildfly.swarm.container.WarDeployment;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

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

    public StaticDeployment(Container container) throws IOException, ModuleLoadException {
        super(container);
    }

    public StaticDeployment(Container container, String contextPath) throws IOException, ModuleLoadException {
        super(container, contextPath);
    }

    @Override
    public WebArchive getArchive() {
        this.archive.addAsWebInfResource(new StringAsset("path-prefix['/'] -> static-content[]"), "undertow-handlers.conf");
        this.archive.addAsWebInfResource(new StringAsset(JBOSS_DEPLOYMENT_STRUCTURE_CONTENTS), "jboss-deployment-structure.xml");
        this.archive.addAsServiceProvider("io.undertow.server.handlers.builder.HandlerBuilder", "org.wildfly.swarm.runtime.undertow.StaticHandlerBuilder");
        return super.getArchive();
    }

}
