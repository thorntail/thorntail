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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.wildfly.swarm.bootstrap.env.DependencyTree;
import org.wildfly.swarm.bootstrap.util.MavenArtifactDescriptor;

/**
 * The declaration of direct and transient dependencies declared by a project build.
 * These maye be unresolved, which means you cannot assume they are available in any local repository.
 *
 * @author Heiko Braun
 * @author Ken Finnigan
 * @since 24/10/2016
 */
public class DeclaredDependencies extends DependencyTree<ArtifactSpec> {

    public Collection<ArtifactSpec> getExplicitDependencies() {
        return getDirectDeps();
    }

    public Set<ArtifactSpec> getTransientDependencies() {
        if (null == allTransient) {
            allTransient = new HashSet<>();
            for (ArtifactSpec directDep : getDirectDeps()) {
                allTransient.addAll(getTransientDependencies(directDep));
            }
        }
        return allTransient;
    }

    public Collection<ArtifactSpec> getTransientDependencies(ArtifactSpec artifact) {
        return getTransientDeps(artifact);
    }

    /**
     * 'Presolved' means a build component (i.e. mojo) pre-computed the transient dependencies
     * and thus we can assume this set is fully and correctly resolved
     *
     * @return
     */
    public boolean isPresolved() {
        return getTransientDependencies().size() > 0;
    }

    public static ArtifactSpec createSpec(String gav) {
        return createSpec(gav, "compile");
    }

    public static ArtifactSpec createSpec(String gav, String scope) {

        try {
            MavenArtifactDescriptor maven = ArtifactSpec.fromMavenGav(gav);
            return new ArtifactSpec(
                    scope,
                    maven.groupId(),
                    maven.artifactId(),
                    maven.version(),
                    maven.type(),
                    null,
                    null
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public void writeTo(File file) {
        try {
            Writer w = new FileWriter(file);
            for (ArtifactSpec key : depTree.keySet()) {
                w.write(key.mavenGav());
                w.write(":\n");
                for (ArtifactSpec s : depTree.get(key)) {
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
    protected int comparator(ArtifactSpec first, ArtifactSpec second) {
        if (first.scope.equals("compile") || first.scope.equals("provided")) {
            return -1;
        } else if (second.scope.equals("compile") || second.scope.equals("provided")) {
            return 1;
        }
        return 0;
    }

    private HashSet<ArtifactSpec> allTransient;
}
