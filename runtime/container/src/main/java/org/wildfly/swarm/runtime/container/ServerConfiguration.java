package org.wildfly.swarm.runtime.container;

import java.util.List;

import org.jboss.dmr.ModelNode;
import org.wildfly.swarm.container.Fraction;

/**
 * @author Bob McWhirter
 */
public interface ServerConfiguration<T extends Fraction> {

    Class<T> getType();

    T defaultFraction();

    List<ModelNode> getList(T fraction);

}
