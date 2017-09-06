package org.wildfly.swarm.opentracing.runtime;

import io.opentracing.contrib.web.servlet.filter.TracingFilter;
import org.jboss.logging.Logger;
import org.jboss.shrinkwrap.api.Archive;
import org.wildfly.swarm.opentracing.OpenTracingFraction;
import org.wildfly.swarm.spi.api.DeploymentProcessor;
import org.wildfly.swarm.spi.runtime.annotations.DeploymentScoped;
import org.wildfly.swarm.undertow.WARArchive;
import org.wildfly.swarm.undertow.descriptors.WebXmlAsset;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;

/**
 * @author Juraci Paixão Kröhling
 */
@DeploymentScoped
public class OpenTracingInstaller implements DeploymentProcessor {
    private static final Logger logger = Logger.getLogger(OpenTracingInstaller.class);
    private final Archive<?> archive;

    @Inject
    private Instance<OpenTracingFraction> openTracingFraction;

    @Inject
    public OpenTracingInstaller(Archive archive) {
        this.archive = archive;
    }

    @Override
    public void process() throws Exception {
        OpenTracingFraction fraction = openTracingFraction.get();

        logger.info("Determining whether to install OpenTracing integration or not.");
        if (archive.getName().endsWith(".war")) {
            logger.logf(Logger.Level.INFO, "Installing the OpenTracing integration for the deployment %s", archive.getName());
            WARArchive webArchive = archive.as(WARArchive.class);
            WebXmlAsset webXml = webArchive.findWebXmlAsset();

            logger.logf(Logger.Level.INFO, "Adding the listener org.wildfly.swarm.opentracing.deployment.OpenTracingInitializer");
            webXml.addListener("org.wildfly.swarm.opentracing.deployment.OpenTracingInitializer");

            setContextParamIfNotNull(webXml, TracingFilter.SKIP_PATTERN, fraction.getServletSkipPattern());
        }
    }

    private void setContextParamIfNotNull(WebXmlAsset webXml, String key, String value) {
        if (value == null || value.isEmpty()) {
            return;
        }

        webXml.setContextParam(key, value);
    }
}
