package org.wildfly.swarm.runtime.container;

import org.jboss.dmr.ModelNode;
import org.wildfly.swarm.container.Fraction;

import java.util.List;

/**
 * @author Bob McWhirter
 */
public interface ServerConfiguration<T extends Fraction> {

    Class<T> getType();
    T defaultFraction();
    List<ModelNode> getList(T fraction);

}
