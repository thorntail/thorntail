/**
 * Copyright 2015-2017 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
            if (path.contains("thorntail-runner")) {
                remove.add(node.getPath());
            }
            if (resolvedDependencies.isRemovable(node)) {
                remove.add(node.getPath());
            }
        }

        for (Node each : node.getChildren()) {
            filter(remove, each, resolvedDependencies);
        }
    }
}
