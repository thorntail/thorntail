package org.wildfly.swarm.container.runtime.internal;

import java.util.List;

import org.jboss.dmr.ModelNode;
import org.wildfly.swarm.config.runtime.invocation.Marshaller;
import org.wildfly.swarm.spi.api.Fraction;

/**
 * @author Bob McWhirter
 */
public class MarshalConfigurator implements Configurator {

    @Override
    public void execute(Fraction fraction, List<ModelNode> list) throws Exception {
        list.addAll(Marshaller.marshal(fraction));
    }
}
