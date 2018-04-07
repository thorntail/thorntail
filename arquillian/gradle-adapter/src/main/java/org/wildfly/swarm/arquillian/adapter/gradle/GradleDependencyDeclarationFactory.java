/**
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

import java.io.File;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

import org.wildfly.swarm.arquillian.adapter.DependencyDeclarationFactory;
import org.wildfly.swarm.arquillian.resolver.ShrinkwrapArtifactResolvingHelper;
import org.wildfly.swarm.bootstrap.util.MavenArtifactDescriptor;
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

        DeclaredDependencies declaredDependencies = gradleAdapter.parseDependencies(GradleDependencyAdapter.Configuration.TEST_RUNTIME);

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
        // The Shrinkwrap resolving helper returns a new collection of artifacts and doesn't update the existing ones.
        // Looks like the code contracts broke at some point in time. For now, let us just iterate through the collection twice.
        Map<String, File> resolvedArtifactMap = helper.resolveAll(collection, false, true).stream()
                .collect(Collectors.toMap(MavenArtifactDescriptor::mavenGav, s -> s.file));
        collection.parallelStream().forEach(s -> {
            resolvedArtifactMap.computeIfPresent(s.mavenGav(), (k, file) -> s.file = file);
        });
    }
}
