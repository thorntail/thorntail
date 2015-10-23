package org.wildfly.swarm.jca.runtime;

import java.util.List;

import org.jboss.dmr.ModelNode;
import org.wildfly.swarm.config.runtime.invocation.Marshaller;
import org.wildfly.swarm.container.runtime.AbstractServerConfiguration;
import org.wildfly.swarm.jca.JCAFraction;

/**
 * @author Bob McWhirter
 * @author Lance Ball
 */
public class JCAConfiguration extends AbstractServerConfiguration<JCAFraction> {

    public JCAConfiguration() {
        super(JCAFraction.class);
    }

    @Override
    public JCAFraction defaultFraction() {
        return JCAFraction.createDefaultFraction();
    }

    @Override
    public List<ModelNode> getList(JCAFraction fraction) throws Exception {
        return Marshaller.marshal(fraction);
    }
}
