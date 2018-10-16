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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.DependencySet;
import org.gradle.api.artifacts.ModuleVersionIdentifier;
import org.gradle.api.artifacts.ResolvedArtifact;
import org.gradle.api.artifacts.ResolvedConfiguration;
import org.gradle.api.artifacts.ResolvedDependency;
import org.gradle.api.internal.artifacts.dependencies.DefaultDependencyArtifact;
import org.gradle.api.internal.artifacts.dependencies.DefaultExternalModuleDependency;
import org.gradle.api.internal.artifacts.dependencies.DefaultProjectDependency;
import org.gradle.api.internal.project.DefaultProjectAccessListener;
import org.gradle.api.internal.project.ProjectInternal;
import org.gradle.api.plugins.ExtraPropertiesExtension;
import org.gradle.initialization.ProjectAccessListener;
import org.wildfly.swarm.fractions.FractionDescriptor;
import org.wildfly.swarm.tools.ArtifactSpec;

/**
 * The {@code GradleDependencyResolutionHelper} helps with resolving and translating a project's dependencies in to various
 * forms that are usable by the Thorntail tooling for Gradle.
 */
public final class GradleDependencyResolutionHelper {

    private static String pluginVersion;

    private GradleDependencyResolutionHelper() {
    }

    /**
     * Parse the plugin definition file and extract the version details from it.
     */
    public static String determinePluginVersion() {
        if (pluginVersion == null) {
            final String fileName = "META-INF/gradle-plugins/thorntail.properties";
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            String version;
            try (InputStream stream = loader.getResourceAsStream(fileName)) {
                Properties props = new Properties();
                props.load(stream);
                version = props.getProperty("implementation-version");
            } catch (IOException e) {
                throw new IllegalStateException("Unable to locate file: " + fileName, e);
            }
            pluginVersion = version;
        }
        return pluginVersion;
    }

    /**
     * Get the collection of Gradle projects along with their GAV definitions. This collection is used for determining if an
     * artifact specification represents a Gradle project or not.
     *
     * @param project the Gradle project that is being analyzed.
     * @return a map of GAV coordinates for each of the available projects (returned as keys).
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Project> getAllProjects(final Project project) {
        return getCachedReference(project, "thorntail_project_gav_collection", () -> {
            Map<String, Project> gavMap = new HashMap<>();
            project.getRootProject().getAllprojects().forEach(p -> {
                gavMap.put(p.getGroup() + ":" + p.getName() + ":" + p.getVersion(), p);
            });
            return gavMap;
        });
    }

    /**
     * Resolve the given artifact specifications.
     *
     * @param project         the Gradle project reference.
     * @param specs           the specifications that need to be resolved.
     * @param transitive      should the artifacts be resolved transitively?
     * @param excludeDefaults should we skip resolving artifacts that belong to the Thorntail group?
     * @return collection of resolved artifact specifications.
     */
    public static Set<ArtifactSpec> resolveArtifacts(Project project, Collection<ArtifactSpec> specs, boolean transitive, boolean excludeDefaults) {
        if (project == null) {
            throw new IllegalArgumentException("Gradle project reference cannot be null.");
        }
        if (specs == null) {
            project.getLogger().warn("Artifact specification collection is null.");
            return Collections.emptySet();
        }

        final Configuration config = project.getConfigurations().detachedConfiguration().setTransitive(transitive);
        final DependencySet dependencySet = config.getDependencies();
        final Map<String, Project> projectGAVCoordinates = getAllProjects(project);
        final ProjectAccessListener listener = new DefaultProjectAccessListener();

        Set<ArtifactSpec> result = new HashSet<>();
        specs.forEach(s -> {
            // 1. Do we need to resolve this entry?
            final String specGAV = String.format("%s:%s:%s", s.groupId(), s.artifactId(), s.version());
            boolean resolved = s.file != null;
            boolean projectEntry = projectGAVCoordinates.containsKey(specGAV);

            // 2. Should we skip this spec?
            if (excludeDefaults && FractionDescriptor.THORNTAIL_GROUP_ID.equals(s.groupId()) && !projectEntry) {
                return;
            }

            // 3. Should this entry be resolved?
            if (!resolved || transitive) {
                // a.) Does this entry represent a project dependency?
                if (projectGAVCoordinates.containsKey(specGAV)) {
                    dependencySet.add(new DefaultProjectDependency((ProjectInternal) projectGAVCoordinates.get(specGAV), listener, false));
                } else {
                    DefaultExternalModuleDependency d = new DefaultExternalModuleDependency(s.groupId(), s.artifactId(), s.version());
                    DefaultDependencyArtifact da = new DefaultDependencyArtifact(s.artifactId(), s.type(), s.type(), s.classifier(), null);
                    d.addArtifact(da);
                    dependencySet.add(d);
                }
            } else {
                // 4. Nothing else to do, just add the spec to the result.
                result.add(s);
            }
        });

        // 5. Are there any specs that need resolution?
        if (!dependencySet.isEmpty()) {
            config.getResolvedConfiguration().getResolvedArtifacts().stream()
                    .map(ra -> asDescriptor(ra).toArtifactSpec())
                    .forEach(result::add);
        }
        return result;
    }

    /**
     * Determine the dependencies associated with a Gradle project. This method returns a Map whose key represents a top level
     * dependency associated with this project and the value represents a collection of dependencies that the "key" requires.
     *
     * @param project       the Gradle project reference.
     * @param configuration the dependency configuration that needs to be resolved.
     * @return the dependencies associated with the Gradle project.
     */
    public static Map<DependencyDescriptor, Set<DependencyDescriptor>> determineProjectDependencies(Project project, String configuration) {
        if (project == null) {
            throw new IllegalArgumentException("Gradle project reference cannot be null.");
        }
        project.getLogger().info("Requesting dependencies for configuration: {}", configuration);
        ResolvedConfiguration resolvedConfig = project.getConfigurations().getByName(configuration).getResolvedConfiguration();
        Map<DependencyDescriptor, Set<DependencyDescriptor>> dependencyMap = resolvedConfig.getFirstLevelModuleDependencies()
                .stream()
                .collect(Collectors.toMap(GradleDependencyResolutionHelper::asDescriptor,
                                          d -> d.getChildren()
                                                  .stream()
                                                  .map(GradleDependencyResolutionHelper::asDescriptor)
                                                  .collect(Collectors.toSet())));

        printDependencyMap(dependencyMap, project);
        return dependencyMap;
    }

    private static DependencyDescriptor asDescriptor(ResolvedDependency dependency) {
        // When using Spring's dependency-management-plugin, some of the artifact specs will end up with a scope of either
        // "master" or "default" which is not honored by ScopeType. This is observed when executing Arquillian based tests cases.
        //  We need to rewrite the scope to "compile".
        final List<String> REWRITE_SCOPES = Arrays.asList("master", "default");

        Set<ResolvedArtifact> artifacts = dependency.getModuleArtifacts();

        // Let us use the first artifact's type for determining the type.
        // I am not sure under what circumstances, would we need to check for multiple artifacts.
        String type = "jar";
        String classifier = null;
        File file = null;
        String scope = dependency.getConfiguration();
        if (REWRITE_SCOPES.contains(scope)) {
            scope = "compile";
        }

        if (!artifacts.isEmpty()) {
            ResolvedArtifact ra = artifacts.iterator().next();
            type = ra.getType();
            classifier = ra.getClassifier();
            file = ra.getFile();
        }
        return new DefaultDependencyDescriptor(scope, dependency.getModuleGroup(), dependency.getModuleName(),
                                               dependency.getModuleVersion(), type, classifier, file);
    }

    /**
     * Translate the given resolved artifact in to a Dependency descriptor.
     */
    private static DependencyDescriptor asDescriptor(ResolvedArtifact artifact) {
        ModuleVersionIdentifier id = artifact.getModuleVersion().getId();
        return new DefaultDependencyDescriptor("default", id.getGroup(), id.getName(), id.getVersion(), artifact.getType(),
                                               artifact.getClassifier(), artifact.getFile());
    }

    /**
     * Temp method for printing out the dependency map.
     */
    private static void printDependencyMap(Map<DependencyDescriptor, Set<DependencyDescriptor>> map, Project project) {
        StringBuilder builder = new StringBuilder(100);
        map.forEach((k, v) -> {
            builder.append(k).append("\n");
            v.forEach(e -> builder.append("\t").append(e).append("\n"));
            builder.append("\n");
        });
        project.getLogger().info("Resolved dependencies:\n" + builder.toString());
    }

    /**
     * Get data (identified by the given key) that has been cached on the given Gradle project reference.
     *
     * @param project  the Gradle project reference.
     * @param key      the key used for caching the data.
     * @param supplier the function that needs to be executed in the event of a cache-miss.
     * @param <T>      the type of the data stored on the project.
     * @return data that has been cached on the given Gradle project reference.
     */
    @SuppressWarnings("unchecked")
    private static <T> T getCachedReference(Project project, String key, Supplier<T> supplier) {
        if (project == null) {
            throw new IllegalArgumentException("Gradle project reference cannot be null.");
        }

        Project rootProject = project.getRootProject();
        ExtraPropertiesExtension ext = rootProject.getExtensions().getExtraProperties();
        T value;
        if (ext.has(key)) {
            value = (T) ext.get(key);
        } else {
            value = supplier.get();
            ext.set(key, value);
        }
        return value;
    }
}
