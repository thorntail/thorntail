package org.wildfly.swarm.jaeger.runtime;

import org.jboss.logging.Logger;
import org.jboss.shrinkwrap.api.Archive;
import org.wildfly.swarm.jaeger.JaegerFraction;
import org.wildfly.swarm.spi.api.DeploymentProcessor;
import org.wildfly.swarm.spi.runtime.annotations.DeploymentScoped;
import org.wildfly.swarm.undertow.WARArchive;
import org.wildfly.swarm.undertow.descriptors.WebXmlAsset;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import static com.uber.jaeger.Configuration.*;

/**
 * @author Juraci Paixão Kröhling
 */
@DeploymentScoped
public class JaegerInstaller implements DeploymentProcessor {
    private static final Logger logger = Logger.getLogger(JaegerInstaller.class);

    private final Archive<?> archive;

    @Inject
    private Instance<JaegerFraction> jaegerFractionInstance;

    @Inject
    public JaegerInstaller(Archive archive) {
        this.archive = archive;
    }

    @Override
    public void process() throws Exception {
        JaegerFraction fraction = jaegerFractionInstance.get();
        logger.info("Determining whether to install Jaeger integration or not.");
        logger.info("JaegerFraction instance: " + fraction);

        if (archive.getName().endsWith(".war")) {
            logger.logf(Logger.Level.INFO, "Installing the Jaeger integration for the deployment %s", archive.getName());
            WARArchive webArchive = archive.as(WARArchive.class);
            WebXmlAsset webXml = webArchive.findWebXmlAsset();

            logger.logf(Logger.Level.INFO, "Adding the listener org.wildfly.swarm.jaeger.deployment.JaegerInitializer");
            webXml.addListener("org.wildfly.swarm.jaeger.deployment.JaegerInitializer");

            setContextParamIfNotNull(webXml, JAEGER_SERVICE_NAME, fraction.getServiceName());
            setContextParamIfNotNull(webXml, JAEGER_SERVICE_NAME, fraction.getServiceName());
            setContextParamIfNotNull(webXml, JAEGER_SAMPLER_TYPE, fraction.getSamplerType());
            setContextParamIfNotNull(webXml, JAEGER_SAMPLER_PARAM, fraction.getSamplerParameter());
            setContextParamIfNotNull(webXml, JAEGER_SAMPLER_MANAGER_HOST_PORT, fraction.getSamplerManagerHost());
            setContextParamIfNotNull(webXml, JAEGER_REPORTER_LOG_SPANS, fraction.getReporterLogSpans());
            setContextParamIfNotNull(webXml, JAEGER_AGENT_HOST, fraction.getAgentHost());
            setContextParamIfNotNull(webXml, JAEGER_AGENT_PORT, fraction.getAgentPort());
            setContextParamIfNotNull(webXml, JAEGER_REPORTER_FLUSH_INTERVAL, fraction.getReporterFlushInterval());
            setContextParamIfNotNull(webXml, JAEGER_REPORTER_MAX_QUEUE_SIZE, fraction.getReporterMaxQueueSize());
            webXml.setContextParam("skipOpenTracingResolver", "true");

            if (fraction.isB3HeaderPropagationEnabled()) {
                webXml.setContextParam("enableB3HeaderPropagation", "true");
            }
        }
    }

    private void setContextParamIfNotNull(WebXmlAsset webXml, String key, String value) {
        if (value == null || value.isEmpty()) {
            return;
        }

        webXml.setContextParam(key, value);
    }
}
