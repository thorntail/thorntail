package org.wildfly.swarm.container.runtime;

import java.util.Collections;
import java.util.List;

import org.jboss.dmr.ModelNode;
import org.jboss.msc.service.ServiceActivator;
import org.jboss.shrinkwrap.api.Archive;
import org.wildfly.swarm.container.Fraction;

/**
 * @author Bob McWhirter
 */
public interface ServerConfiguration<T extends Fraction> {

    Class<T> getType();

    T defaultFraction();

    default List<ServiceActivator> getServiceActivators(T fraction) {
        return Collections.emptyList();
    }

    default void prepareArchive(Archive a) {

    }

    default List<Archive> getImplicitDeployments(T fraction) {
        return Collections.emptyList();
    }

    default List<ModelNode> getList(T fraction) {
        return Collections.emptyList();
    }

    default boolean isIgnorable() {
        return false;
    }

}
