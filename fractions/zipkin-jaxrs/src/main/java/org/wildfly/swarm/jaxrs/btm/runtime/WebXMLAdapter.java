package org.wildfly.swarm.jaxrs.btm.runtime;

import org.jboss.shrinkwrap.api.Archive;
import org.wildfly.swarm.jaxrs.JAXRSArchive;
import org.wildfly.swarm.jaxrs.btm.zipkin.ServerRequestInterceptor;
import org.wildfly.swarm.jaxrs.btm.zipkin.ServerResponseInterceptor;
import org.wildfly.swarm.spi.api.ArchivePreparer;

/**
 * @author Heiko Braun
 * @since 17/10/16
 */
public class WebXMLAdapter implements ArchivePreparer {

    private static final String SERVER_SIDE_FILTERS =
            ServerRequestInterceptor.class.getName() + ","
                    + ServerResponseInterceptor.class.getName();

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
