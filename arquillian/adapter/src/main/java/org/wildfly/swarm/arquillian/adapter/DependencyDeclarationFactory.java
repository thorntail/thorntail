package org.wildfly.swarm.arquillian.adapter;

import org.wildfly.swarm.arquillian.resolver.ShrinkwrapArtifactResolvingHelper;
import org.wildfly.swarm.internal.FileSystemLayout;
import org.wildfly.swarm.internal.MavenFileSystemLayout;
import org.wildfly.swarm.tools.DeclaredDependencies;

/**
 * @author Heiko Braun
 * @since 26/10/2016
 */
abstract class DependencyDeclarationFactory {

    abstract DeclaredDependencies create(ShrinkwrapArtifactResolvingHelper resolvingHelper);

    public static DependencyDeclarationFactory newInstance(FileSystemLayout fsLayout) {
        if (fsLayout instanceof MavenFileSystemLayout) {
            return new MavenDependencyDeclarationFactory();
        } else {
            return new GradleDependencyDeclarationFactory(fsLayout);
        }
    }
}
