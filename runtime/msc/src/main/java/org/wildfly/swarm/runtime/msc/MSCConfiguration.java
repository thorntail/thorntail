package org.wildfly.swarm.runtime.msc;

import java.util.ArrayList;
import java.util.List;

import org.jboss.dmr.ModelNode;
import org.wildfly.swarm.msc.MSCFraction;
import org.wildfly.swarm.runtime.container.AbstractServerConfiguration;

/**
 * @author Bob McWhirter
 */
public class MSCConfiguration extends AbstractServerConfiguration<MSCFraction> {

    public MSCConfiguration() {
        super(MSCFraction.class);
    }

    @Override
    public MSCFraction defaultFraction() {
        return new MSCFraction();
    }

    @Override
    public List<ModelNode> getList(MSCFraction fraction) {
        List<ModelNode> list = new ArrayList<>();
        return list;
    }
}
