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

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.gradle.tooling.GradleConnectionException;
import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.ProjectConnection;
import org.gradle.tooling.model.ExternalDependency;
import org.gradle.tooling.model.GradleModuleVersion;
import org.gradle.tooling.model.idea.IdeaModule;
import org.gradle.tooling.model.idea.IdeaModuleDependency;
import org.gradle.tooling.model.idea.IdeaProject;
import org.wildfly.swarm.plugin.gradle.DependencyDescriptor;
import org.wildfly.swarm.plugin.gradle.GradleToolingHelper;
import org.wildfly.swarm.plugin.gradle.ThorntailConfiguration;
import org.wildfly.swarm.tools.ArtifactSpec;
import org.wildfly.swarm.tools.DeclaredDependencies;

import static java.util.Collections.emptyMap;
import static java.util.Collections.emptySet;

/**
 * Get the dependency details for a Gradle project. This adapter makes use of the IdeaProject definition that is provided by
 * default with every Gradle installation in order to determine the target set of dependencies.
 *
 * @author Heiko Braun
 * @since 19/10/16
 */
public class GradleDependencyAdapter {

    public GradleDependencyAdapter(Path projectDir) {
        this.rootPath = projectDir;
    }

    @SuppressWarnings("UnstableApiUsage")
    public DeclaredDependencies getProjectDependencies() {
        return PREVIOUSLY_COMPUTED_VALUES.computeIfAbsent(rootPath, __ -> {
            DeclaredDependencies declaredDependencies;

            File projectDir = rootPath.toFile();
            GradleConnector connector = GradleConnector.newConnector().forProjectDirectory(projectDir);
            ProjectConnection connection = connector.connect();
            try {
                // 1. Attempt to fetch the dependencies via the Thorntail model.
                declaredDependencies = getDependenciesViaThorntailModel(connection);

                if (declaredDependencies == null) {
                    System.err.println("The 'thorntail-arquillian' plugin is missing on your project. " +
                                               "Falling back to IdeaProject model which may not give accurate results.");
                    // Fallback and load the dependencies via the Idea project model.
                    declaredDependencies = getDependenciesViaIdeaModel(connection);
                }
            } finally {
                connection.close();
            }

            return declaredDependencies;
        });
    }

    /**
     * Get the dependencies via the Gradle IDEA model.
     *
     * @param connection the Gradle project connection.
     * @return the computed dependencies.
     */
    private DeclaredDependencies getDependenciesViaIdeaModel(ProjectConnection connection) {
        DeclaredDependencies declaredDependencies = new DeclaredDependencies();

        // 1. Get the IdeaProject model from the Gradle connection.
        IdeaProject prj = connection.getModel(IdeaProject.class);
        prj.getModules().forEach(this::computeProjectDependencies);

        // 2. Find the IdeaModule that maps to the project that we are looking at.
        Optional<? extends IdeaModule> prjModule = prj.getModules().stream()
                .filter(m -> m.getGradleProject().getProjectDirectory().toPath().equals(rootPath))
                .findFirst();


        // We need to return the following collection of dependencies,
        // 1. For the current project, return all artifacts that are marked as ["COMPILE", "TEST"]
        // 2. From the upstream-projects, add all dependencies that are marked as ["COMPILE"]
        // 3. For the current project, iterate through each dependencies marked as ["PROVIDED"] and do the following,
        //      a.) Check if they are already available from 1 & 2. If yes, then nothing to do here.
        //      b.) Check if this entry is defined as ["TEST"] in the upstream-projects. If yes, then include it.
        //          -- The reason for doing this is because of the optimization that Gradle does on the IdeaModule library set.
        Set<ArtifactSpec> collectedDependencies = new HashSet<>();
        prjModule.ifPresent(m -> {
            Map<String, Set<ArtifactSpec>> currentPrjDeps = ARTIFACT_DEPS_OF_PRJ.get(m.getName());
            Set<String> upstreamProjects = PRJ_DEPS_OF_PRJ.getOrDefault(m.getName(), emptySet());

            collectedDependencies.addAll(currentPrjDeps.getOrDefault(DEP_SCOPE_COMPILE, emptySet()));
            collectedDependencies.addAll(currentPrjDeps.getOrDefault(DEP_SCOPE_TEST, emptySet()));

            upstreamProjects.forEach(moduleName -> {
                Map<String, Set<ArtifactSpec>> moduleDeps = ARTIFACT_DEPS_OF_PRJ.getOrDefault(moduleName, emptyMap());
                collectedDependencies.addAll(moduleDeps.getOrDefault(DEP_SCOPE_COMPILE, emptySet()));
            });

            Set<ArtifactSpec> providedScopeDeps = currentPrjDeps.getOrDefault(DEP_SCOPE_PROVIDED, emptySet());
            providedScopeDeps.removeAll(collectedDependencies);

            if (!providedScopeDeps.isEmpty()) {
                List<ArtifactSpec> testScopedLibs = new ArrayList<>();

                upstreamProjects.forEach(moduleName -> testScopedLibs.addAll(
                        ARTIFACT_DEPS_OF_PRJ.getOrDefault(moduleName, emptyMap())
                                .getOrDefault(DEP_SCOPE_TEST, emptySet())));
                providedScopeDeps.stream().filter(testScopedLibs::contains).forEach(collectedDependencies::add);
            }

        });

        collectedDependencies.forEach(declaredDependencies::add);
        return declaredDependencies;
    }

    /**
     * Compute the dependencies of a given {@code IdeaModule} and group them by their scope.
     *
     * Note: This method does not follow project->project dependencies. It just makes a note of them in a separate collection.
     *
     * @param module the IdeaModule reference.
     */
    @SuppressWarnings("UnstableApiUsage")
    private void computeProjectDependencies(IdeaModule module) {
        ARTIFACT_DEPS_OF_PRJ.computeIfAbsent(module.getName(), moduleName -> {
            Map<String, Set<ArtifactSpec>> dependencies = new HashMap<>();
            module.getDependencies().forEach(dep -> {
                if (dep instanceof IdeaModuleDependency) {
                    // Add the dependency to the list.
                    String name = ((IdeaModuleDependency) dep).getTargetModuleName();
                    PRJ_DEPS_OF_PRJ.computeIfAbsent(moduleName, key -> new HashSet<>()).add(name);
                } else if (dep instanceof ExternalDependency) {
                    ExternalDependency extDep = (ExternalDependency) dep;
                    GradleModuleVersion gav = extDep.getGradleModuleVersion();
                    ArtifactSpec spec = new ArtifactSpec("compile", gav.getGroup(), gav.getName(), gav.getVersion(),
                                                         "jar", null, extDep.getFile());
                    String depScope = dep.getScope().getScope();
                    dependencies.computeIfAbsent(depScope, s -> new HashSet<>()).add(spec);
                }
            });
            return dependencies;
        });
    }

    /**
     * Attempt to load the {@link ThorntailConfiguration} and retrieve the dependencies.
     *
     * @param connection the Gradle project connection.
     * @return the declared dependencies for the current project.
     */
    private DeclaredDependencies getDependenciesViaThorntailModel(ProjectConnection connection) {
        DeclaredDependencies declaredDependencies = null;
        try {
            ThorntailConfiguration config = connection.getModel(ThorntailConfiguration.class);
            if (config != null) {
                declaredDependencies = GradleToolingHelper.toDeclaredDependencies(config.getTestDependencies());
            }

        } catch (GradleConnectionException | IllegalStateException e) {
            e.printStackTrace();
        }
        return declaredDependencies;
    }

    /**
     * Translate the given dependency descriptor to the an artifact specification while overriding the scope as well.
     *
     * @param desc  the dependency descriptor reference.
     * @param scope the scope that needs to be set on the result.
     * @return an artifact sepcification.
     */
    private static ArtifactSpec toArtifactSpec(DependencyDescriptor desc, String scope) {
        return new ArtifactSpec(scope, desc.getGroup(), desc.getName(), desc.getVersion(), desc.getType(),
                                desc.getClassifier(), desc.getFile());
    }

    private Path rootPath;

    // For a given Gradle build, we should be computing the dependencies only once per project.
    // This cache improves the build speed.
    private static final Map<Path, DeclaredDependencies> PREVIOUSLY_COMPUTED_VALUES = new HashMap<>();

    // What are the artifacts that a given project depends on?
    // Map < module-name, Map< scope, collection of artifacts > >
    private static final Map<String, Map<String, Set<ArtifactSpec>>> ARTIFACT_DEPS_OF_PRJ = new HashMap<>();

    // What are the projects that a given project depends on?
    private static final Map<String, Set<String>> PRJ_DEPS_OF_PRJ = new HashMap<>();

    ///
    /// Scopes used by the Idea Project model of Gradle.
    ///
    private static final String DEP_SCOPE_COMPILE = "COMPILE";

    private static final String DEP_SCOPE_TEST = "TEST";

    private static final String DEP_SCOPE_PROVIDED = "PROVIDED";

    // private static final String DEP_SCOPE_RUNTIME = "RUNTIME";
}
