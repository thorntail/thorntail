package org.wildfly.swarm.container.runtime.cdi.configurable;

import java.io.IOException;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import org.jboss.jandex.IndexView;
import org.jboss.jandex.Indexer;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ArchivePath;
import org.jboss.shrinkwrap.api.Node;
import org.wildfly.swarm.container.runtime.cdi.DeploymentContext;
import org.wildfly.swarm.spi.runtime.annotations.DeploymentScoped;

/**
 * Created by bob on 5/15/17.
 */
@ApplicationScoped
public class DeploymentProducer {

    private static final String CLASS_SUFFIX = ".class";

    @Inject
    DeploymentContext context;

    @Produces
    @DeploymentScoped
    @Default
    Archive archive() {
        return context.getCurrentArchive();
    }

    @Produces
    @DeploymentScoped
    @Default
    IndexView index() {
        Indexer indexer = new Indexer();
        Map<ArchivePath, Node> c = context.getCurrentArchive().getContent();
        try {
            for (Map.Entry<ArchivePath, Node> each : c.entrySet()) {
                if (each.getKey().get().endsWith(CLASS_SUFFIX)) {
                    indexer.index(each.getValue().getAsset().openStream());
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return indexer.complete();
    }
}
