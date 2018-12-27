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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Stack;
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
     * Resolve the given artifact specifications.
     *
     * @param project         the Gradle project reference.
     * @param specs           the specifications that need to be resolved.
     * @param transitive      should the artifacts be resolved transitively?
     * @param excludeDefaults should we skip resolving artifacts that belong to the Thorntail group?
     * @return collection of resolved artifact specifications.
     */
    public static Set<ArtifactSpec> resolveArtifacts(Project project, Collection<ArtifactSpec> specs, boolean transitive,
                                                     boolean excludeDefaults) {
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
                    .map(ra -> asDescriptor("compile", ra).toArtifactSpec())
                    .forEach(result::add);
        }
        return result;
    }

    /**
     * Determine the dependencies associated with a Gradle project. This method returns a Map whose key represents a top level
     * dependency associated with this project and the value represents a collection of dependencies that the "key" requires.
     *
     * @param project                     the Gradle project reference.
     * @param configuration               the dependency configuration that needs to be resolved.
     * @param resolveChildrenTransitively if set to true, then upstream dependencies will be resolved transitively.
     * @return the dependencies associated with the Gradle project for the specified configuration.
     */
    public static Map<DependencyDescriptor, Set<DependencyDescriptor>>
    determineProjectDependencies(Project project, String configuration, boolean resolveChildrenTransitively) {
        if (project == null) {
            throw new IllegalArgumentException("Gradle project reference cannot be null.");
        }
        project.getLogger().info("Requesting dependencies for configuration: {}", configuration);
        Configuration requestedConfiguration = project.getConfigurations().findByName(configuration);
        if (requestedConfiguration == null) {
            project.getLogger().warn("Unable to locate dependency configuration with name: {}", configuration);
            return Collections.emptyMap();
        }

        //
        // Step 1
        // ------
        // Iterate through the hierarchy of the given configuration and determine the correct scope of all
        // "top-level" dependencies.
        //
        Map<String, String> dependencyScopeMap = new HashMap<>();

        // In case of custom configurations, we will assign the scope to what has been requested
        String defaultScopeForUnknownConfigurations =
                REMAPPED_SCOPES.computeIfAbsent(requestedConfiguration.getName(), cfgName -> {
                    throw new IllegalStateException("Unknown configuration name provided: " + cfgName);
                });
        requestedConfiguration.getHierarchy().forEach(cfg -> {
            cfg.getDependencies().forEach(dep -> {
                String key = String.format("%s:%s", dep.getGroup(), dep.getName());
                dependencyScopeMap.put(key, REMAPPED_SCOPES.getOrDefault(cfg.getName(), defaultScopeForUnknownConfigurations));
            });
        });

        //
        // Step 2
        // ------
        // Assuming that the given configuration can be resolved, get the resolved artifacts and populate the return Map.
        //
        ResolvedConfiguration resolvedConfig = requestedConfiguration.getResolvedConfiguration();
        Map<DependencyDescriptor, Set<DependencyDescriptor>> dependencyMap = new HashMap<>();
        resolvedConfig.getFirstLevelModuleDependencies().forEach(resolvedDep -> {
            String lookup = String.format("%s:%s", resolvedDep.getModuleGroup(), resolvedDep.getModuleName());
            String scope = dependencyScopeMap.get(lookup);
            if (scope == null) {
                // Should never happen.
                throw new IllegalStateException("Gradle dependency resolution logic is broken. Unable to get scope for dependency: " + lookup);
            }
            DependencyDescriptor key = asDescriptor(scope, resolvedDep);
            Set<DependencyDescriptor> value;
            if (resolveChildrenTransitively) {
                value = getDependenciesTransitively(scope, resolvedDep);
            } else {
                value = resolvedDep.getChildren()
                        .stream()
                        .map(rd -> asDescriptor(scope, rd))
                        .collect(Collectors.toSet());
            }
            dependencyMap.put(key, value);
        });

        printDependencyMap(dependencyMap, project);
        return dependencyMap;
    }

    /**
     * Get the dependencies (transitively) of the given parent element.
     *
     * @param scope  the scope to use for the parent.
     * @param parent the parent dependency.
     * @return a collection of all dependencies of the given parent.
     */
    private static Set<DependencyDescriptor> getDependenciesTransitively(String scope, ResolvedDependency parent) {
        Stack<ResolvedDependency> stack = new Stack<>();
        stack.push(parent);
        Set<DependencyDescriptor> dependencies = new HashSet<>();
        while (!stack.empty()) {
            ResolvedDependency rd = stack.pop();
            rd.getModuleArtifacts().forEach(a -> dependencies.add(asDescriptor(scope, a)));
            rd.getChildren().forEach(d -> {
                if (!stack.contains(d)) {
                    stack.add(d);
                }
            });
        }
        return dependencies;
    }

    /**
     * Translate the given {@link ResolvedDependency resolved dependency} in to a {@link DependencyDescriptor} reference.
     *
     * @param scope      the scope to assign to the descriptor.
     * @param dependency the resolved dependency reference.
     * @return an instance of {@link DependencyDescriptor}.
     */
    private static DependencyDescriptor asDescriptor(String scope, ResolvedDependency dependency) {

        Set<ResolvedArtifact> artifacts = dependency.getModuleArtifacts();

        // Let us use the first artifact's type for determining the type.
        // I am not sure under what circumstances, would we need to check for multiple artifacts.
        String type = "jar";
        String classifier = null;
        File file = null;

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
     * Translate the given {@link ResolvedArtifact resolved artifact} in to a {@link DependencyDescriptor} reference.
     *
     * @param scope    the scope to assign to the descriptor.
     * @param artifact the resolved artifact reference.
     * @return an instance of {@link DependencyDescriptor}.
     */
    private static DependencyDescriptor asDescriptor(String scope, ResolvedArtifact artifact) {
        ModuleVersionIdentifier id = artifact.getModuleVersion().getId();
        return new DefaultDependencyDescriptor(scope, id.getGroup(), id.getName(), id.getVersion(),
                                               artifact.getType(), artifact.getClassifier(), artifact.getFile());
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
     * Get the collection of Gradle projects along with their GAV definitions. This collection is used for determining if an
     * artifact specification represents a Gradle project or not.
     *
     * @param project the Gradle project that is being analyzed.
     * @return a map of GAV coordinates for each of the available projects (returned as keys).
     */
    @SuppressWarnings("unchecked")
    private static Map<String, Project> getAllProjects(final Project project) {
        return getCachedReference(project, "thorntail_project_gav_collection", () -> {
            Map<String, Project> gavMap = new HashMap<>();
            project.getRootProject().getAllprojects().forEach(p -> {
                gavMap.put(p.getGroup() + ":" + p.getName() + ":" + p.getVersion(), p);
            });
            return gavMap;
        });
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

    // Translate the different Gradle dependency scopes in to values that map to ShrinkWrap's scope type.
    // c.f.: https://docs.gradle.org/current/userguide/java_plugin.html#sec:java_plugin_and_dependency_management
    private static final Map<String, String> REMAPPED_SCOPES = new HashMap<>();

    static {
        // Avoid compiler warning.
        final String COMPILE = "compile";
        final String TEST = "test";
        final String PROVIDED = "provided";
        final String RUNTIME = "runtime";

        REMAPPED_SCOPES.put("compile", COMPILE);
        REMAPPED_SCOPES.put("api", COMPILE);
        REMAPPED_SCOPES.put("implementation", COMPILE);
        REMAPPED_SCOPES.put("apiElements", COMPILE);
        REMAPPED_SCOPES.put("compileClasspath", COMPILE);

        REMAPPED_SCOPES.put("providedCompile", PROVIDED);
        REMAPPED_SCOPES.put("compileOnly", PROVIDED);

        REMAPPED_SCOPES.put("runtime", RUNTIME);
        REMAPPED_SCOPES.put("runtimeOnly", RUNTIME);
        REMAPPED_SCOPES.put("runtimeElements", RUNTIME);
        REMAPPED_SCOPES.put("runtimeClasspath", RUNTIME);
        REMAPPED_SCOPES.put("providedRuntime", RUNTIME);

        REMAPPED_SCOPES.put("testImplementation", TEST);
        REMAPPED_SCOPES.put("testCompileOnly", TEST);
        REMAPPED_SCOPES.put("testRuntimeOnly", TEST);
        REMAPPED_SCOPES.put("testCompile", TEST);
        REMAPPED_SCOPES.put("testCompileClasspath", TEST);
        REMAPPED_SCOPES.put("testRuntime", TEST);
        REMAPPED_SCOPES.put("testRuntimeClasspath", TEST);

        // When using Spring's dependency-management-plugin, some of the artifact specs will end up with a scope of either
        // "master" or "default" which is not honored by ScopeType. This is observed when executing Arquillian based tests cases.
        //  We need to rewrite the scope to "compile".
        REMAPPED_SCOPES.put("default", COMPILE);
        REMAPPED_SCOPES.put("master", COMPILE);
    }
}
