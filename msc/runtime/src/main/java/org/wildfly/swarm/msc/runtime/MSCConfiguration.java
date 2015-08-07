package org.wildfly.swarm.msc.runtime;

import java.util.ArrayList;
import java.util.List;

import org.jboss.dmr.ModelNode;
import org.wildfly.swarm.container.runtime.AbstractServerConfiguration;
import org.wildfly.swarm.msc.MSCFraction;

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
