package org.wildfly.swarm.runtime.weld;

import org.jboss.dmr.ModelNode;
import org.wildfly.swarm.runtime.container.AbstractServerConfiguration;
import org.wildfly.swarm.weld.WeldJAXRSFraction;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Bob McWhirter
 */
public class WeldJAXRSConfiguration extends AbstractServerConfiguration<WeldJAXRSFraction> {

    public WeldJAXRSConfiguration() {
        super(WeldJAXRSFraction.class);
    }

    @Override
    public WeldJAXRSFraction defaultFraction() {
        return new WeldJAXRSFraction();
    }

    @Override
    public List<ModelNode> getList(WeldJAXRSFraction fraction) {
        List<ModelNode> list = new ArrayList<>();
        return list;

    }
}
