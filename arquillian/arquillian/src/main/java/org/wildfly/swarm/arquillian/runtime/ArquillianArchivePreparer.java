package org.wildfly.swarm.arquillian.runtime;

import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.wildfly.swarm.msc.ServiceActivatorArchive;
import org.wildfly.swarm.spi.api.ArchivePreparer;
import org.wildfly.swarm.spi.api.JARArchive;

/**
 * @author Bob McWhirter
 */
public class ArquillianArchivePreparer implements ArchivePreparer {

    @Override
    public void prepareArchive(Archive<?> archive) {
        if (archive.get("META-INF/arquillian-testable") == null) {
            return;
        }

        archive.add(new StringAsset(archive.getName()), "META-INF/arquillian-testable");

        archive.as(JARArchive.class)
                .addModule("org.wildfly.swarm.arquillian.adapter");

        archive.as(JARArchive.class)
                .addModule("org.wildfly.swarm.arquillian", "deployment");

        archive.as(ServiceActivatorArchive.class)
                .addServiceActivator("org.wildfly.swarm.arquillian.deployment.TestableArchiveServiceActivator");

    }
}
