package org.wildfly.swarm.opentracing.hawkular.jaxrs.runtime;

import org.jboss.shrinkwrap.api.Archive;
import org.wildfly.swarm.jaxrs.JAXRSArchive;
import org.wildfly.swarm.opentracing.hawkular.jaxrs.filters.TracingDynamicFeature;
import org.wildfly.swarm.spi.api.DeploymentProcessor;
import org.wildfly.swarm.spi.runtime.annotations.DeploymentScoped;

/**
 * @author Pavol Loffay
 */
@DeploymentScoped
public class InstallTracing implements DeploymentProcessor {

    private static final String SERVER_SIDE_FILTERS = TracingDynamicFeature.class.getName();

    public InstallTracing(Archive archive) {
        this.archive = archive;
    }

    @Override
    public void process() {
        if (archive instanceof JAXRSArchive) {
            JAXRSArchive jaxrsArchive = archive.as(JAXRSArchive.class);
            jaxrsArchive.findWebXmlAsset().setContextParam(
                    "resteasy.providers", SERVER_SIDE_FILTERS
            );
        }
    }

    private final Archive archive;
}
