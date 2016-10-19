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
package org.wildfly.swarm.tools;

import java.util.HashSet;
import java.util.Set;

/**
 * The declaration of build artifacts, explicit and transient dependencies declared by a project build.
 * These are all unresolved dependencies, which means you cannot assume they are available in any local repository.
 *
 * @author Heiko Braun
 * @since 24/10/2016
 */
public class DeclaredDependencies {

    public void addExplicitDependency(ArtifactSpec artifactSpec) {
        this.explicitDependencies.add(artifactSpec);
    }

    public void addBuildArtifact(ArtifactSpec artifactSpec) {
        this.buildArtifacts.add(artifactSpec);
    }

    public void addTransientDependency(ArtifactSpec artifactSpec) {
        this.transientDependencies.add(artifactSpec);
    }

    public Set<ArtifactSpec> getBuildArtifacts() {
        return buildArtifacts;
    }

    public Set<ArtifactSpec> getExplicitDependencies() {
        return explicitDependencies;
    }

    public Set<ArtifactSpec> getTransientDependencies() {
        return transientDependencies;
    }

    /**
     * Presolved means a build local component (i.e. mojo) to pre-compute the transient dependencies
     * and thus we can assume this set is fully and correctly resolved
     * @return
     */
    public boolean isPresolved() {
        return getTransientDependencies().size()>0;
    }

    private Set<ArtifactSpec> buildArtifacts = new HashSet<>();
    private Set<ArtifactSpec> explicitDependencies = new HashSet<>();
    private Set<ArtifactSpec> transientDependencies = new HashSet<>();

}
