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
import org.jboss.shrinkwrap.resolver.api.maven.coordinate.MavenCoordinate;
import org.wildfly.swarm.arquillian.resolver.ShrinkwrapArtifactResolvingHelper;
import org.wildfly.swarm.tools.ArtifactSpec;
import org.wildfly.swarm.tools.DeclaredDependencies;

/**
 * @author Heiko Braun
 * @since 26/10/2016
 */
class MavenDependencyDeclarationFactory extends DependencyDeclarationFactory {


    @Override
    public DeclaredDependencies create(ShrinkwrapArtifactResolvingHelper resolvingHelper) {
        final DeclaredDependencies declaredDependencies = new DeclaredDependencies();

        // NonTransitiveStrategy
        final MavenResolvedArtifact[] explicitDeps =
                resolvingHelper.withResolver(r -> MavenProfileLoader.loadPom(r)
                        .importRuntimeAndTestDependencies()
                        .resolve()
                        .withoutTransitivity()
                        .asResolvedArtifact());

        for (MavenResolvedArtifact dep : explicitDeps) {
            MavenCoordinate coord = dep.getCoordinate();
            ArtifactSpec artifactSpec = new ArtifactSpec(
                    dep.getScope().name(), coord.getGroupId(),
                    coord.getArtifactId(), coord.getVersion(),
                    coord.getPackaging().getExtension(), coord.getClassifier(), dep.asFile()
            );
            declaredDependencies.addExplicitDependency(artifactSpec);
        }

        // TransitiveStrategy

        final MavenResolvedArtifact[] presolvedDeps =
                resolvingHelper.withResolver(r -> MavenProfileLoader.loadPom(r)
                        .importRuntimeAndTestDependencies()
                        .resolve()
                        .withTransitivity()
                        .asResolvedArtifact());

        for (MavenResolvedArtifact dep : presolvedDeps) {
            MavenCoordinate coord = dep.getCoordinate();
            ArtifactSpec artifactSpec = new ArtifactSpec(
                    dep.getScope().name(), coord.getGroupId(),
                    coord.getArtifactId(), coord.getVersion(),
                    coord.getPackaging().getExtension(), coord.getClassifier(), dep.asFile()
            );
            declaredDependencies.addTransientDependency(artifactSpec);
        }

        return declaredDependencies;
    }


}
