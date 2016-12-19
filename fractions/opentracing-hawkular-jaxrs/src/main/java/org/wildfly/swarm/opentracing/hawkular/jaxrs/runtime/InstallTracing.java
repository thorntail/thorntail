package org.wildfly.swarm.opentracing.hawkular.jaxrs.runtime;

import org.jboss.shrinkwrap.api.Archive;
import org.wildfly.swarm.jaxrs.JAXRSArchive;
import org.wildfly.swarm.opentracing.hawkular.jaxrs.filters.TracingDynamicFeature;
import org.wildfly.swarm.spi.api.ArchivePreparer;

/**
 * @author Pavol Loffay
 */
public class InstallTracing implements ArchivePreparer {

    private static final String SERVER_SIDE_FILTERS = TracingDynamicFeature.class.getName();

    @Override
    public void prepareArchive(Archive<?> archive) {
        if (archive instanceof JAXRSArchive) {
            JAXRSArchive jaxrsArchive = archive.as(JAXRSArchive.class);
            jaxrsArchive.findWebXmlAsset().setContextParam(
                    "resteasy.providers", SERVER_SIDE_FILTERS
            );
        }
    }
}
