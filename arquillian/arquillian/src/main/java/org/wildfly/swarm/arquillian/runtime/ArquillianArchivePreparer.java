package org.wildfly.swarm.arquillian.runtime;

import javax.inject.Inject;

import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.wildfly.swarm.msc.ServiceActivatorArchive;
import org.wildfly.swarm.spi.api.DeploymentProcessor;
import org.wildfly.swarm.spi.api.JARArchive;
import org.wildfly.swarm.spi.runtime.annotations.DeploymentScoped;

/**
 * @author Bob McWhirter
 */
@DeploymentScoped
public class ArquillianArchivePreparer implements DeploymentProcessor {

    @Inject
    public ArquillianArchivePreparer(Archive archive) {
        this.archive = archive;
    }

    public void process() {
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

    private final Archive archive;
}
