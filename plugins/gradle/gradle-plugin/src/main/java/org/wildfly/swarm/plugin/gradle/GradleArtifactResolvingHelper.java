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
import java.util.Set;

import org.gradle.api.Project;
import org.wildfly.swarm.tools.ArtifactResolvingHelper;
import org.wildfly.swarm.tools.ArtifactSpec;

/**
 * @author Bob McWhirter
 */
public class GradleArtifactResolvingHelper implements ArtifactResolvingHelper {


    private final Project project;

    public GradleArtifactResolvingHelper(Project project) {
        this.project = project;
        this.project.getRepositories().maven(repo -> {
            repo.setName("jboss-public");
            repo.setUrl("https://repository.jboss.org/nexus/content/groups/public/");
        });
        this.project.getRepositories().maven(repo -> {
            repo.setName("redhat-ga");
            repo.setUrl("https://maven.repository.redhat.com/ga/");
        });
    }

    @Override
    public ArtifactSpec resolve(final ArtifactSpec spec) {
        Set<ArtifactSpec> resolved = GradleDependencyResolutionHelper.resolveArtifacts(project, Collections.singleton(spec),
                                                                                       false, false);
        ArtifactSpec result = null;
        if (!resolved.isEmpty()) {
            result = resolved.iterator().next();
            if (result.file == null) {
                result = null;
            }
        }
        return result;
    }

    @Override
    public Set<ArtifactSpec> resolveAll(final Collection<ArtifactSpec> specs, boolean transitive, boolean defaultExcludes) {
        return GradleDependencyResolutionHelper.resolveArtifacts(project, specs, transitive, defaultExcludes);
    }

}
