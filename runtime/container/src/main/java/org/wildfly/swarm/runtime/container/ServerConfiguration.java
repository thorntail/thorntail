package org.wildfly.swarm.runtime.container;

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

    List<ModelNode> getList(T fraction);

    default List<ServiceActivator> getServiceActivators(T fraction) {
        return Collections.emptyList();
    }

    default void prepareArchive(Archive a) {

    }

}
