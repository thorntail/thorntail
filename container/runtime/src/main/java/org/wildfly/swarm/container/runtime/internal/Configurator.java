package org.wildfly.swarm.container.runtime.internal;

import java.util.List;

import org.jboss.dmr.ModelNode;
import org.wildfly.swarm.spi.api.Fraction;

/**
 * @author Bob McWhirter
 */
public interface Configurator {
    void execute(Fraction fraction, List<ModelNode> list) throws Exception;
}
