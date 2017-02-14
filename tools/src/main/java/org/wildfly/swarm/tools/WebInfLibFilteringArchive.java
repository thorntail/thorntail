package org.wildfly.swarm.tools;

import java.util.HashSet;
import java.util.Set;

import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ArchivePath;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.GenericArchive;
import org.jboss.shrinkwrap.api.Node;
import org.jboss.shrinkwrap.impl.base.GenericArchiveImpl;
import org.wildfly.swarm.spi.api.JBossDeploymentStructureContainer;

/**
 * @author Bob McWhirter
 */
public class WebInfLibFilteringArchive extends GenericArchiveImpl
        implements JBossDeploymentStructureContainer<GenericArchive> {

    public WebInfLibFilteringArchive(Archive<?> archive, ResolvedDependencies resolvedDependencies) {
        super(archive);
        filter(resolvedDependencies);
        addModule(BuildTool.APP_DEPENDENCY_MODULE);
    }

    protected void filter(ResolvedDependencies resolvedDependencies) {
        Set<ArchivePath> remove = new HashSet<>();
        filter(remove, getArchive().get(ArchivePaths.root()), resolvedDependencies);

        for (ArchivePath each : remove) {
            getArchive().delete(each);
        }
    }

    protected void filter(Set<ArchivePath> remove, Node node, ResolvedDependencies resolvedDependencies) {
        String path = node.getPath().get();
        if (path.startsWith("/WEB-INF/lib") && path.endsWith(".jar")) {
            // all libs will be purged and instead resolved from BuildTool.APP_DEPENDENCY_MODULE
            remove.add(node.getPath());
        }

        for (Node each : node.getChildren()) {
            filter(remove, each, resolvedDependencies);
        }
    }
}
