/*
 * Copyright 2015-2016 Red Hat, Inc, and individual contributors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wildfly.swarm.arquillian.adapter.gradle;

import java.util.Collection;

import org.wildfly.swarm.arquillian.adapter.DependencyDeclarationFactory;
import org.wildfly.swarm.arquillian.resolver.ShrinkwrapArtifactResolvingHelper;
import org.wildfly.swarm.internal.FileSystemLayout;
import org.wildfly.swarm.internal.GradleFileSystemLayout;
import org.wildfly.swarm.tools.ArtifactSpec;
import org.wildfly.swarm.tools.DeclaredDependencies;

/**
 * @author Heiko Braun
 * @since 26/10/2016
 */
public class GradleDependencyDeclarationFactory implements DependencyDeclarationFactory {

    @Override
    public DeclaredDependencies create(FileSystemLayout fsLayout, ShrinkwrapArtifactResolvingHelper resolvingHelper) {

        GradleDependencyAdapter gradleAdapter = new GradleDependencyAdapter(fsLayout.getRootPath());

        DeclaredDependencies declaredDependencies = gradleAdapter.getProjectDependencies();

        // Resolve to local files.
        resolveDependencies(declaredDependencies.getRuntimeExplicitAndTransientDependencies(), resolvingHelper);
        return declaredDependencies;
    }

    @Override
    public boolean acceptsFsLayout(FileSystemLayout fsLayout) {
        return fsLayout instanceof GradleFileSystemLayout;
    }

    /**
     * Resolve the given collection of ArtifactSpec references. This method attempts the resolution and ensures that the
     * references are updated to be as complete as possible.
     *
     * @param collection the collection artifact specifications.
     */
    private static void resolveDependencies(Collection<ArtifactSpec> collection, ShrinkwrapArtifactResolvingHelper helper) {
        // Identify the artifact specs that need resolution.
        // Ideally, there should be none at this point.
        collection.forEach(spec -> {
            if (spec.file == null) {
                // Resolve it.
                ArtifactSpec resolved = helper.resolve(spec);
                if (resolved != null) {
                    spec.file = resolved.file;
                } else {
                    throw new IllegalStateException("Unable to resolve artifact: " + spec.toString());
                }
            }
        });
    }
}
