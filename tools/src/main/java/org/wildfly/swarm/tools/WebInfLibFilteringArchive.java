package org.wildfly.swarm.tools;

import java.util.HashSet;
import java.util.Set;

import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ArchivePath;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.Node;
import org.jboss.shrinkwrap.impl.base.GenericArchiveImpl;

/**
 * @author Bob McWhirter
 */
public class WebInfLibFilteringArchive extends GenericArchiveImpl {

    public WebInfLibFilteringArchive(Archive<?> archive, DependencyManager dependencyManager) {
        super(archive);
        filter(dependencyManager);
    }

    protected void filter(DependencyManager dependencyManager) {
        Set<ArchivePath> remove = new HashSet<>();
        filter(remove, getArchive().get(ArchivePaths.root()), dependencyManager);

        for (ArchivePath each : remove) {
            getArchive().delete(each);
        }
    }

    protected void filter(Set<ArchivePath> remove, Node node, DependencyManager dependencyManager) {
        String path = node.getPath().get();
        if (path.startsWith("/WEB-INF/lib") && path.endsWith(".jar")) {
            if (dependencyManager.isRemovable(node)) {
                remove.add(node.getPath());
            }
        }

        for (Node each : node.getChildren()) {
            filter(remove, each, dependencyManager);
        }
    }
}
