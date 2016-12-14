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

import java.util.HashSet;

import org.wildfly.swarm.arquillian.resolver.ShrinkwrapArtifactResolvingHelper;
import org.wildfly.swarm.internal.FileSystemLayout;
import org.wildfly.swarm.tools.ArtifactSpec;
import org.wildfly.swarm.tools.DeclaredDependencies;

/**
 * @author Heiko Braun
 * @since 26/10/2016
 */
public class GradleDependencyDeclarationFactory extends DependencyDeclarationFactory {

    public GradleDependencyDeclarationFactory(FileSystemLayout fsLayout) {
        this.fsLayout = fsLayout;
    }

    @Override
    public DeclaredDependencies create(ShrinkwrapArtifactResolvingHelper resolvingHelper) {

        GradleDependencyAdapter gradleAdapter = new GradleDependencyAdapter(fsLayout.getRootPath());

        DeclaredDependencies declaredDependencies = gradleAdapter.parseDependencies(GradleDependencyAdapter.Configuration.TEST_RUNTIME);

        // resolve to local files
        resolvingHelper.resolveAll(new HashSet<ArtifactSpec>(declaredDependencies.getTransientDependencies()), false, false);

        return declaredDependencies;
    }

    private final FileSystemLayout fsLayout;
}
