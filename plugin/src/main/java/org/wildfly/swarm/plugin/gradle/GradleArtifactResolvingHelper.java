/**
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
import java.util.Set;
import java.util.stream.Collectors;

import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.DependencySet;
import org.gradle.api.artifacts.ResolvedDependency;
import org.gradle.api.artifacts.repositories.MavenArtifactRepository;
import org.gradle.api.internal.artifacts.DefaultExcludeRule;
import org.gradle.api.internal.artifacts.dependencies.DefaultDependencyArtifact;
import org.gradle.api.internal.artifacts.dependencies.DefaultExternalModuleDependency;
import org.wildfly.swarm.tools.ArtifactResolvingHelper;
import org.wildfly.swarm.tools.ArtifactSpec;

/**
 * @author Bob McWhirter
 */
public class GradleArtifactResolvingHelper implements ArtifactResolvingHelper {


    private final Project project;

    public GradleArtifactResolvingHelper(Project project) {
        this.project = project;
        this.project.getRepositories().maven(new Action<MavenArtifactRepository>() {
            @Override
            public void execute(MavenArtifactRepository repo) {
                repo.setName("jboss-public");
                repo.setUrl("http://repository.jboss.org/nexus/content/groups/public/");
            }
        });
    }


    @Override
    public ArtifactSpec resolve(final ArtifactSpec spec) {
        if (spec.file != null) {
            return spec;
        }

        final Iterator<ResolvedDependency> iterator =
                doResolve(new HashSet<>(Collections.singletonList(spec))).iterator();
        if (iterator.hasNext()) {
            spec.file = iterator.next()
                    .getModuleArtifacts()
                    .iterator().next()
                    .getFile();

            return spec;
        }

        return null;
    }

    @Override
    public Set<ArtifactSpec> resolveAll(final Set<ArtifactSpec> specs) throws Exception {
        if (specs.isEmpty()) {

            return specs;
        }

        final Set<ArtifactSpec> resolvedSpecs = new HashSet<>();

        doResolve(specs).forEach(dep -> dep.getAllModuleArtifacts()
                .forEach(artifact -> resolvedSpecs
                        .add(new ArtifactSpec(dep.getConfiguration(),
                                              dep.getModuleGroup(),
                                              artifact.getName(),
                                              dep.getModuleVersion(),
                                              artifact.getExtension(),
                                              artifact.getClassifier(),
                                              artifact.getFile()))));

        return resolvedSpecs.stream()
                .filter(a -> !"system".equals(a.scope))
                .collect(Collectors.toSet());
    }

    private Set<ResolvedDependency> doResolve(final Collection<ArtifactSpec> deps) {
        final Configuration config = this.project.getConfigurations().detachedConfiguration();
        final DependencySet dependencySet = config.getDependencies();

        deps.forEach(spec -> {
            final DefaultExternalModuleDependency d =
                    new DefaultExternalModuleDependency(spec.groupId(), spec.artifactId(), spec.version());
            final DefaultDependencyArtifact da =
                    new DefaultDependencyArtifact(spec.artifactId(), spec.type(), spec.type(), spec.classifier(), null);
            d.addArtifact(da);
            d.getExcludeRules().add(new DefaultExcludeRule());
            dependencySet.add(d);
        });

        return config.getResolvedConfiguration().getFirstLevelModuleDependencies();
    }

}
