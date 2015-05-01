package org.wildfly.swarm.msc;

import org.jboss.dmr.ModelNode;
import org.wildfly.swarm.container.AbstractFraction;

import java.util.Collections;
import java.util.List;

/**
 * @author Bob McWhirter
 */
public class MSCFraction extends AbstractFraction {

    public MSCFraction() {

    }

    @Override
    public List<ModelNode> getList() {
        return Collections.emptyList();
    }
}
