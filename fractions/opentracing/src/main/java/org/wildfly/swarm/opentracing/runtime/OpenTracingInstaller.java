package org.wildfly.swarm.opentracing.runtime;

import org.jboss.logging.Logger;
import org.jboss.shrinkwrap.api.Archive;
import org.wildfly.swarm.spi.api.DeploymentProcessor;
import org.wildfly.swarm.spi.runtime.annotations.DeploymentScoped;
import org.wildfly.swarm.undertow.WARArchive;
import org.wildfly.swarm.undertow.descriptors.WebXmlAsset;

import javax.inject.Inject;

/**
 * @author Juraci Paixão Kröhling
 */
@DeploymentScoped
public class OpenTracingInstaller implements DeploymentProcessor {
    private static final Logger logger = Logger.getLogger(OpenTracingInstaller.class);
    private final Archive<?> archive;

    @Inject
    public OpenTracingInstaller(Archive archive) {
        this.archive = archive;
    }

    @Override
    public void process() throws Exception {
        logger.info("Determining whether to install OpenTracing integration or not.");
        if (archive.getName().endsWith(".war")) {
            logger.logf(Logger.Level.INFO, "Installing the OpenTracing integration for the deployment %s", archive.getName());
            WARArchive webArchive = archive.as(WARArchive.class);
            WebXmlAsset webXml = webArchive.findWebXmlAsset();

            logger.logf(Logger.Level.INFO, "Adding the listener org.wildfly.swarm.opentracing.deployment.OpenTracingInitializer");
            webXml.addListener("org.wildfly.swarm.opentracing.deployment.OpenTracingInitializer");
        }
    }
}
