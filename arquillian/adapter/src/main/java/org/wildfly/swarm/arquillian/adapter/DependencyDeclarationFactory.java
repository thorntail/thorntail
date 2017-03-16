package org.wildfly.swarm.arquillian.adapter;

import org.wildfly.swarm.arquillian.resolver.ShrinkwrapArtifactResolvingHelper;
import org.wildfly.swarm.internal.FileSystemLayout;
import org.wildfly.swarm.tools.DeclaredDependencies;

import java.util.ServiceLoader;

/**
 * @author Heiko Braun
 * @since 26/10/2016
 */
public interface DependencyDeclarationFactory {

    DeclaredDependencies create(FileSystemLayout fsLayout, ShrinkwrapArtifactResolvingHelper resolvingHelper);

    boolean acceptsFsLayout(FileSystemLayout fsLayout);

    static DependencyDeclarationFactory newInstance(FileSystemLayout fsLayout) {
        ServiceLoader<DependencyDeclarationFactory> factoryIterable = ServiceLoader.load(DependencyDeclarationFactory.class);
        for (DependencyDeclarationFactory factory : factoryIterable) {
            if (factory.acceptsFsLayout(fsLayout)) {
                return factory;
            }
        }

        throw new IllegalArgumentException("Unsupported FileSystemLayout: " + fsLayout);
    }
}
