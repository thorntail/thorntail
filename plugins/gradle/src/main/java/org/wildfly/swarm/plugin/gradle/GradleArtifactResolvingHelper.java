/*
 * Copyright 2015-2016 Red Hat, Inc, and individual contributors.
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
package org.wildfly.swarm.plugin.gradle;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.DependencySet;
import org.gradle.api.artifacts.ResolvedArtifact;
import org.gradle.api.internal.artifacts.dependencies.DefaultDependencyArtifact;
import org.gradle.api.internal.artifacts.dependencies.DefaultExternalModuleDependency;
import org.gradle.api.internal.artifacts.dependencies.DefaultProjectDependency;
import org.gradle.api.internal.project.DefaultProjectAccessListener;
import org.gradle.api.internal.project.ProjectInternal;
import org.wildfly.swarm.tools.ArtifactResolvingHelper;
import org.wildfly.swarm.tools.ArtifactSpec;

/**
 * @author Bob McWhirter
 */
public class GradleArtifactResolvingHelper implements ArtifactResolvingHelper {


    private final Project project;

    Map<String, Project> projects;

    public GradleArtifactResolvingHelper(Project project) {
        this.project = project;
        this.projects = project.getRootProject().getAllprojects().stream().collect(Collectors.toMap(p -> p.getGroup() + ":" + p.getName() + ":" + p.getVersion(), p -> p));
        this.project.getRepositories().maven(repo -> {
            repo.setName("jboss-public");
            repo.setUrl("https://repository.jboss.org/nexus/content/groups/public/");
        });
    }

    @Override
    public ArtifactSpec resolve(final ArtifactSpec spec) {
        if (spec.file != null) {
            return spec;
        }

        final Iterator<ResolvedArtifact> iterator =
                doResolve(new HashSet<>(Collections.singletonList(spec)), false).iterator();
        if (iterator.hasNext()) {
            spec.file = iterator.next().getFile();

            return spec;
        }

        return null;
    }

    @Override
    public Set<ArtifactSpec> resolveAll(final Collection<ArtifactSpec> specs, boolean transitive, boolean defaultExcludes) throws Exception {
        if (specs.isEmpty()) {
            return Collections.emptySet();
        }

        // If we do not need to resolve transitively, then just check if the artifacts have a file associated or not.
        // If missing, then fetch the artifacts from the dependency resolver.
        if (!transitive) {
            Set<ArtifactSpec> needResolution = specs.stream().filter(a -> a.file == null).collect(Collectors.toSet());
            needResolution.forEach(this::resolve);

            // Defensive check to see if the resolution behavior was changed inadvertently.
            // Ensure that all the artifacts have a file identified as part of the resolution.
            Set<ArtifactSpec> set = needResolution.stream().filter(a -> a.file == null).collect(Collectors.toSet());
            if (!set.isEmpty()) {
                throw new IllegalStateException("Internal Tooling Error: Looks like all artifacts were not resolved: " + set);
            }
            return specs instanceof Set ? (Set<ArtifactSpec>) specs : new HashSet<>(specs);
        }

        return doResolve(specs, transitive)
                .stream()
                .map(artifact -> new ArtifactSpec("default",
                                                  artifact.getModuleVersion().getId().getGroup(),
                                                  artifact.getModuleVersion().getId().getName(),
                                                  artifact.getModuleVersion().getId().getVersion(),
                                                  artifact.getExtension(),
                                                  artifact.getClassifier(),
                                                  artifact.getFile()))
                .collect(Collectors.toSet());
    }

    private Collection<ResolvedArtifact> doResolve(final Collection<ArtifactSpec> deps, boolean transitive) {
        final Configuration config = this.project.getConfigurations().detachedConfiguration().setTransitive(transitive);
        final DependencySet dependencySet = config.getDependencies();

        deps.forEach(spec -> {
            if (projects.containsKey(spec.groupId() + ":" + spec.artifactId() + ":" + spec.version())) {
                dependencySet.add(new DefaultProjectDependency((ProjectInternal) projects.get(spec.groupId() + ":" + spec.artifactId() + ":" + spec.version()), new DefaultProjectAccessListener(), false));
            } else {
                final DefaultExternalModuleDependency d =
                        new DefaultExternalModuleDependency(spec.groupId(), spec.artifactId(), spec.version());
                final DefaultDependencyArtifact da =
                        new DefaultDependencyArtifact(spec.artifactId(), spec.type(), spec.type(), spec.classifier(), null);
                d.addArtifact(da);
                dependencySet.add(d);
            }
        });

        if (transitive) {
            return config
                    .getResolvedConfiguration()
                    .getResolvedArtifacts();
        }

        return config
                .getResolvedConfiguration()
                .getFirstLevelModuleDependencies()
                .stream()
                .map(dep -> dep.getModuleArtifacts())
                .flatMap(artifacts -> artifacts.stream())
                .collect(Collectors.toList());
    }

}
