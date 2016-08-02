package org.wildfly.swarm.javafx;

import javax.inject.Singleton;

import org.jboss.shrinkwrap.api.Archive;
import org.wildfly.swarm.spi.api.JARArchive;
import org.wildfly.swarm.spi.api.ArchivePreparer;

/**
 * @author Ken Finnigan
 */
@Singleton
public class JavaFXArchivePreparer implements ArchivePreparer {
    @Override
    public void prepareArchive(Archive<?> archive) {
        archive.as(JARArchive.class).addModule("javafx");
    }
}
