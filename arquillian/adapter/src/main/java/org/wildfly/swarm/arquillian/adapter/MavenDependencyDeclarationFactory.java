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
package org.wildfly.swarm.arquillian.adapter;

import org.jboss.shrinkwrap.resolver.api.maven.MavenResolvedArtifact;
import org.jboss.shrinkwrap.resolver.api.maven.PomEquippedResolveStage;
import org.wildfly.swarm.arquillian.resolver.ShrinkwrapArtifactResolvingHelper;
import org.wildfly.swarm.internal.FileSystemLayout;
import org.wildfly.swarm.internal.MavenFileSystemLayout;
import org.wildfly.swarm.tools.ArtifactSpec;
import org.wildfly.swarm.tools.DeclaredDependencies;

/**
 * @author Heiko Braun
 * @since 26/10/2016
 */
public class MavenDependencyDeclarationFactory implements DependencyDeclarationFactory {


    @Override
    public DeclaredDependencies create(FileSystemLayout ignored, ShrinkwrapArtifactResolvingHelper resolvingHelper) {
        final DeclaredDependencies declaredDependencies = new DeclaredDependencies();

        final PomEquippedResolveStage pom = MavenProfileLoader.loadPom(resolvingHelper.getResolver());

        // NonTransitiveStrategy
        final MavenResolvedArtifact[] explicitDeps =
                resolvingHelper.withResolver(r -> pom
                        .importRuntimeAndTestDependencies()
                        .resolve()
                        .withoutTransitivity()
                        .asResolvedArtifact()
                );

        // TransitiveStrategy
        for (MavenResolvedArtifact directDep : explicitDeps) {

            ArtifactSpec parent = new ArtifactSpec(
                    directDep.getScope().toString(),
                    directDep.getCoordinate().getGroupId(),
                    directDep.getCoordinate().getArtifactId(),
                    directDep.getCoordinate().getVersion(),
                    directDep.getCoordinate().getPackaging().toString(),
                    directDep.getCoordinate().getClassifier(),
                    directDep.asFile()
            );
            MavenResolvedArtifact[] bucket =
                    resolvingHelper.withResolver(r -> {
                                                     r.addDependency(resolvingHelper.createMavenDependency(parent));
                                                     return pom
                                                             .resolve()
                                                             .withTransitivity()
                                                             .asResolvedArtifact();
                                                 }
                    );

            for (MavenResolvedArtifact dep : bucket) {

                ArtifactSpec child = new ArtifactSpec(
                        dep.getScope().toString(),
                        dep.getCoordinate().getGroupId(),
                        dep.getCoordinate().getArtifactId(),
                        dep.getCoordinate().getVersion(),
                        dep.getCoordinate().getPackaging().toString(),
                        dep.getCoordinate().getClassifier(),
                        dep.asFile()
                );

                declaredDependencies.add(
                        parent,
                        child
                );
            }
        }

        return declaredDependencies;
    }

    @Override
    public boolean acceptsFsLayout(FileSystemLayout fsLayout) {
        return fsLayout instanceof MavenFileSystemLayout;
    }
}
