package org.wildfly.swarm.container;

import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.container.LibraryContainer;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.wildfly.swarm.Swarm;

import java.util.List;

/**
 * @author Bob McWhirter
 */
public interface DependenciesContainer<T extends Archive<T>> extends LibraryContainer<T>, Archive<T> {

    default T addAllDependencies() throws Exception {
        List<JavaArchive> artifacts = Swarm.allArtifacts();
        addAsLibraries( artifacts );
        return (T) this;
    }
}
