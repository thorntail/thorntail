package org.wildfly.swarm.jaxrs.runtime;

import org.jboss.shrinkwrap.api.ArchiveEvent;
import org.jboss.shrinkwrap.api.ArchiveEventHandler;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.wildfly.swarm.jaxrs.JAXRSArchive;

/**
 * Created by bob on 6/12/17.
 */
public class ApplicationHandler implements ArchiveEventHandler {

    public ApplicationHandler(JAXRSArchive archive, String path) {
        this.archive = archive;
        this.path = path;
    }

    @Override
    public void handle(ArchiveEvent event) {
        Asset asset = event.getAsset();
        if ((DefaultApplicationDeploymentProcessor.PATH_WEB_XML.equals(event.getPath()) && DefaultApplicationDeploymentProcessor.hasApplicationServletMapping(asset))
                || DefaultApplicationDeploymentProcessor.hasApplicationPathAnnotation(event.getPath(), asset)) {
            this.archive.delete(this.path);
        }
    }

    private final JAXRSArchive archive;

    private final String path;
}
