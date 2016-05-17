package org.wildfly.swarm.javafx.runtime;

import org.jboss.shrinkwrap.api.Archive;
import org.wildfly.swarm.javafx.JavaFXFraction;
import org.wildfly.swarm.spi.api.JARArchive;
import org.wildfly.swarm.spi.runtime.AbstractServerConfiguration;

/**
 * @author Ken Finnigan
 */
public class JavaFXConfiguration extends AbstractServerConfiguration<JavaFXFraction> {
    public JavaFXConfiguration() {
        super(JavaFXFraction.class);
    }

    @Override
    public void prepareArchive(Archive<?> archive) {
        archive.as(JARArchive.class).addModule("javafx");
    }
}
