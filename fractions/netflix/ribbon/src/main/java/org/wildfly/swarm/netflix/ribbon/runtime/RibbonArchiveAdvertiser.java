package org.wildfly.swarm.netflix.ribbon.runtime;

import javax.inject.Inject;

import org.jboss.shrinkwrap.api.Archive;
import org.wildfly.swarm.netflix.ribbon.RibbonArchive;
import org.wildfly.swarm.spi.api.DeploymentProcessor;
import org.wildfly.swarm.spi.runtime.annotations.DeploymentScoped;

/**
 * @author Ken Finnigan
 */
@DeploymentScoped
public class RibbonArchiveAdvertiser implements DeploymentProcessor {

    @Inject
    public RibbonArchiveAdvertiser(Archive archive) {
        this.archive = archive;
    }

    @Override
    public void process() {
        // If there hasn't been any services advertised, then advertise a service under the archive name
        if (!archive.as(RibbonArchive.class).hasAdvertised()) {
            archive.as(RibbonArchive.class).advertise();
        }
    }

    private final Archive<?> archive;
}
