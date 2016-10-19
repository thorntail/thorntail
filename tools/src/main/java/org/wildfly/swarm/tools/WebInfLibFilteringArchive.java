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

    public WebInfLibFilteringArchive(Archive<?> archive, ResolvedDependencies resolvedDependencies) {
        super(archive);
        filter(resolvedDependencies);
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
            if (resolvedDependencies.isRemovable(node)) {
                remove.add(node.getPath());
            }
        }

        for (Node each : node.getChildren()) {
            filter(remove, each, resolvedDependencies);
        }
    }
}
