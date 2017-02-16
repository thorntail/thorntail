package org.wildfly.swarm.netflix.ribbon.runtime;

import javax.enterprise.context.ApplicationScoped;

import org.jboss.shrinkwrap.api.Archive;
import org.wildfly.swarm.netflix.ribbon.RibbonArchive;
import org.wildfly.swarm.spi.api.ArchivePreparer;

/**
 * @author Ken Finnigan
 */
@ApplicationScoped
public class RibbonArchiveAdvertiser implements ArchivePreparer {
    @Override
    public void prepareArchive(Archive<?> archive) {
        // If there hasn't been any services advertised, then advertise a service under the archive name
        if (!archive.as(RibbonArchive.class).hasAdvertised()) {
            archive.as(RibbonArchive.class).advertise();
        }
    }
}
