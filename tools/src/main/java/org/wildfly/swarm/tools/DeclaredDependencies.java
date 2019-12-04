/*
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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.wildfly.swarm.bootstrap.env.DependencyTree;
import org.wildfly.swarm.bootstrap.util.MavenArtifactDescriptor;

/**
 * The declaration of direct and transitive dependencies declared by a project build.
 * These maye be unresolved, which means you cannot assume they are available in any local repository.
 *
 * @author Heiko Braun
 * @author Ken Finnigan
 * @since 24/10/2016
 */
public class DeclaredDependencies extends DependencyTree<ArtifactSpec> {

    /**
     * Get the collection of direct dependencies defined in this instance.
     *
     * @return the collection of direct dependencies defined in this instance.
     */
    public Collection<ArtifactSpec> getDirectDependencies() {
        return new LinkedHashSet<>(getDirectDeps());
    }

    /**
     * Get the collection of all transitive dependencies defined in this instance.
     *
     * @return the collection of all transitive dependencies defined in this instance.
     */
    public Set<ArtifactSpec> getTransientDependencies() {
        Set<ArtifactSpec> deps = new HashSet<>();
        this.getDirectDeps().stream()
                .filter(d -> !isThorntailRunner(d))
                .forEach(d -> deps.addAll(this.getTransientDeps(d)));
        return deps;
    }

    /**
     * Get the collection of all dependencies associated with this instance (direct & transient) excluding those marked as test.
     *
     * @return the collection of all dependencies associated with this instance excluding those marked as test.
     */
    public Collection<ArtifactSpec> getRuntimeExplicitAndTransientDependencies() {
        Collection<ArtifactSpec> allDeps = new HashSet<>();
        this.getDirectDeps()
                .stream()
                .filter(spec -> !spec.scope.equals("test"))
                .forEach(spec -> {
                    allDeps.add(spec);
                    allDeps.addAll(this.getTransientDeps(spec));
                });
        return allDeps;
    }

    /**
     * Get the transitive dependencies defined for the given artifact specification.
     *
     * @param artifact the artifact specification.
     * @return the transitive dependencies defined for the given artifact specification.
     */
    public Collection<ArtifactSpec> getTransientDependencies(ArtifactSpec artifact) {
        Set<ArtifactSpec> deps = new HashSet<>();
        if (this.isDirectDep(artifact)) {
            deps.addAll(getTransientDeps(artifact));
        }
        return deps;
    }

    /**
     * 'Presolved' means a build component (i.e. mojo) pre-computed the transient dependencies
     * and thus we can assume this set is fully and correctly resolved
     *
     * @return true if all the dependencies in this instance are pre-solved, false otherwise.
     */
    public boolean isPresolved() {
        // Check if the tool has added unsolved dependencies to this instance that need further resolution.
        boolean unsolvedDependenciesExist = getDirectDeps().stream().anyMatch(d -> !getTransientDeps(d).isEmpty());
        return !unsolvedDependenciesExist;
    }

    /**
     * Get the artifact specification (if available) for Thorntail runner.
     *
     * @return the artifact specification for Thorntail runner.
     */
    public Optional<ArtifactSpec> runnerDependency() {
        return getDirectDependencies().stream().filter(this::isThorntailRunner).findAny();
    }

    /**
     * Determine if the given specification represents a "thorntail-runner" dependency or not.
     *
     * @param artifactSpec the artifact specification.
     * @return true if it represents the thorntail-runner depend
     */
    private boolean isThorntailRunner(ArtifactSpec artifactSpec) {
        return artifactSpec.groupId().equals("io.thorntail") && artifactSpec.artifactId().equals("thorntail-runner");
    }

    /**
     * Create an instance of {@link ArtifactSpec} from the given GAV coordinates. The scope will be set to "compile".
     *
     * @param gav the GAV coordinates for the artifact.
     * @return an instance of {@link ArtifactSpec} from the given GAV coordinates.
     */
    public static ArtifactSpec createSpec(String gav) {
        return createSpec(gav, "compile");
    }

    /**
     * Create an instance of {@link ArtifactSpec} from the given GAV coordinates.
     *
     * @param gav   the GAV coordinates for the artifact.
     * @param scope the scope to be set on the returned instance.
     * @return an instance of {@link ArtifactSpec} from the given GAV coordinates.
     */
    public static ArtifactSpec createSpec(String gav, String scope) {

        try {
            MavenArtifactDescriptor maven = ArtifactSpec.fromMavenGav(gav);
            return new ArtifactSpec(
                    scope,
                    maven.groupId(),
                    maven.artifactId(),
                    maven.version(),
                    maven.type(),
                    maven.classifier(),
                    null
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public void writeTo(File file) {
        try {
            Writer w = new FileWriter(file);
            for (ArtifactSpec key : this.getDirectDeps()) {
                w.write(key.mavenGav());
                w.write(":\n");
                for (ArtifactSpec s : this.getTransientDeps(key)) {
                    w.write("  - ");
                    w.write(s.mavenGav());
                    w.write("\n");
                }
            }
            w.close();
        } catch (IOException e) {
            throw new RuntimeException("Failed to write dependency tree", e);
        }
    }

    @Override
    public Collection<ArtifactSpec> getDirectDeps() {
        // Reorder so that compile and provided come first, but otherwise keep the original order. This is important in
        // case when the final collection is going to be resolved. Different order of direct dependencies may lead to
        // different resolved transitive dependencies.

        Collection<ArtifactSpec> originalDeps = super.getDirectDeps();
        List<ArtifactSpec> reordered = new ArrayList<>(originalDeps.size());
        originalDeps.stream().filter(s -> PRIORITIZED_SCOPES.contains(s.scope)).forEach(reordered::add);
        originalDeps.stream().filter(s -> !PRIORITIZED_SCOPES.contains(s.scope)).forEach(reordered::add);
        return reordered;
    }

    /**
     * Marks given {@code directDep} as "complete", which means that this instance of {@code DeclaredDependencies}
     * already has information about all transitive dependencies brought in by the {@code directDep}. Calling this
     * method only makes sense if the caller has access to the complete dependency tree.
     */
    public void markComplete(ArtifactSpec directDep) {
        if (completeTransitiveDependencies == null) {
            completeTransitiveDependencies = new HashSet<>();
        }
        completeTransitiveDependencies.add(directDep);
    }

    public boolean isComplete(ArtifactSpec directDep) {
        return completeTransitiveDependencies != null && completeTransitiveDependencies.contains(directDep);
    }

    private static final Set<String> PRIORITIZED_SCOPES = Stream.of("compile", "provided").collect(Collectors.toSet());

    private Set<ArtifactSpec> completeTransitiveDependencies;
}
