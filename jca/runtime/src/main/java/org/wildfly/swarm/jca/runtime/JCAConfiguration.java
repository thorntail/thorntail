package org.wildfly.swarm.jca.runtime;

import org.jboss.dmr.ModelNode;
import org.wildfly.apigen.invocation.Marshaller;
import org.wildfly.swarm.container.runtime.AbstractServerConfiguration;
import org.wildfly.swarm.jca.JCAFraction;

import java.util.ArrayList;
import java.util.List;

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
    public List<ModelNode> getList(JCAFraction fraction) {
        try {
            return Marshaller.marshal(fraction);
        } catch (Exception e) {
            System.err.println("Cannot configure JCA subsystem. " + e);
        }
        return new ArrayList<>();
    }
}
