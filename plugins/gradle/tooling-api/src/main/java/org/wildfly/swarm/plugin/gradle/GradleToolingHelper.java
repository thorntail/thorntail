/*
 * Copyright 2018 Red Hat, Inc, and individual contributors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wildfly.swarm.plugin.gradle;

import java.util.Map;
import java.util.Set;

import org.wildfly.swarm.tools.ArtifactSpec;
import org.wildfly.swarm.tools.DeclaredDependencies;

/**
 * This utility acts a bridge between the Gradle tooling model and the Thorntail modules.
 */
public final class GradleToolingHelper {

    private GradleToolingHelper() {
    }

    /**
     * Translate the given dependency tree in to an instance of {@link DeclaredDependencies}.
     */
    public static DeclaredDependencies toDeclaredDependencies(Map<DependencyDescriptor, Set<DependencyDescriptor>> depMap) {
        DeclaredDependencies dependencies = new DeclaredDependencies();
        depMap.forEach((directDep, resolvedDeps) -> {
            ArtifactSpec parent = toArtifactSpec(directDep);
            dependencies.addResolved(parent);
            if (resolvedDeps != null) {
                resolvedDeps.forEach(d -> dependencies.addResolved(parent, toArtifactSpec(d)));
            }
        });
        return dependencies;
    }

    /**
     * Translate the given {@link DependencyDescriptor} in to an instance of {@link ArtifactSpec}.
     */
    public static ArtifactSpec toArtifactSpec(DependencyDescriptor descriptor) {
        return new ArtifactSpec(descriptor.getScope(), descriptor.getGroup(), descriptor.getName(),
                                descriptor.getVersion(), descriptor.getType(), descriptor.getClassifier(),
                                descriptor.getFile());
    }

}
