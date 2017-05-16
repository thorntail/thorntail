package org.wildfly.swarm.jaxrs.btm.runtime;

import javax.inject.Inject;

import org.jboss.shrinkwrap.api.Archive;
import org.wildfly.swarm.jaxrs.JAXRSArchive;
import org.wildfly.swarm.jaxrs.btm.zipkin.ServerRequestInterceptor;
import org.wildfly.swarm.jaxrs.btm.zipkin.ServerResponseInterceptor;
import org.wildfly.swarm.spi.api.DeploymentProcessor;
import org.wildfly.swarm.spi.runtime.annotations.DeploymentScoped;

/**
 * @author Heiko Braun
 * @since 17/10/16
 */
@DeploymentScoped
public class WebXMLAdapter implements DeploymentProcessor {

    private static final String SERVER_SIDE_FILTERS =
            ServerRequestInterceptor.class.getName() + ","
                    + ServerResponseInterceptor.class.getName();

    @Inject
    public WebXMLAdapter(Archive archive) {
        this.archive = archive;
    }

    @Override
    public void process() {
        if (archive.getName().endsWith(".war")) {
            JAXRSArchive jaxrsArchive = archive.as(JAXRSArchive.class);
            jaxrsArchive.findWebXmlAsset().setContextParam(
                    "resteasy.providers", SERVER_SIDE_FILTERS
            );
        }
    }

    private final Archive<?> archive;
}
