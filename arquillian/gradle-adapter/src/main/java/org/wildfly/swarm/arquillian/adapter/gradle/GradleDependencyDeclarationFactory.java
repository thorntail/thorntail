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
package org.wildfly.swarm.arquillian.adapter.gradle;

import org.wildfly.swarm.arquillian.adapter.DependencyDeclarationFactory;
import org.wildfly.swarm.arquillian.resolver.ShrinkwrapArtifactResolvingHelper;
import org.wildfly.swarm.internal.FileSystemLayout;
import org.wildfly.swarm.internal.GradleFileSystemLayout;
import org.wildfly.swarm.tools.DeclaredDependencies;

import java.util.HashSet;

/**
 * @author Heiko Braun
 * @since 26/10/2016
 */
public class GradleDependencyDeclarationFactory implements DependencyDeclarationFactory {

    @Override
    public DeclaredDependencies create(FileSystemLayout fsLayout, ShrinkwrapArtifactResolvingHelper resolvingHelper) {

        GradleDependencyAdapter gradleAdapter = new GradleDependencyAdapter(fsLayout.getRootPath());

        DeclaredDependencies declaredDependencies = gradleAdapter.parseDependencies(GradleDependencyAdapter.Configuration.TEST_RUNTIME);

        // resolve to local files
        resolvingHelper.resolveAll(new HashSet<>(declaredDependencies.getTransientDependencies()), false, false);

        return declaredDependencies;
    }

    @Override
    public boolean acceptsFsLayout(FileSystemLayout fsLayout) {
        return fsLayout instanceof GradleFileSystemLayout;
    }
}
