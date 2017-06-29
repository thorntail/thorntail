package org.wildfly.swarm.container.runtime;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.wildfly.swarm.Swarm;
import org.wildfly.swarm.spi.api.config.ConfigKey;
import org.wildfly.swarm.spi.api.config.ConfigView;
import org.wildfly.swarm.spi.api.config.SimpleKey;

/**
 * Created by bob on 5/24/17.
 */
@ApplicationScoped
public class ArtifactDeployer {

    @Inject
    ConfigView configView;

    @Inject
    private Instance<RuntimeDeployer> deployer;

    public void deploy() throws Exception {
        List<SimpleKey> subkeys = configView.simpleSubkeys(ConfigKey.of("swarm", "deployment"));

        for (SimpleKey subkey : subkeys) {
            String spec = subkey.name();
            if (spec.contains(":")) {
                String[] parts = spec.split(":");
                String groupId = parts[0];
                parts = parts[1].split("\\.");
                String artifactId = parts[0];
                String packaging = parts[1];

                JavaArchive artifact = Swarm.artifact(groupId + ":" + artifactId + ":" + packaging + ":*", artifactId + "." + packaging);
                deployer.get().deploy(artifact, spec);
            }
        }

    }
}
