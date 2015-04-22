package org.wildfly.swarm.container;

import org.jboss.dmr.ModelNode;

import java.util.List;

/**
 * @author Bob McWhirter
 */
public interface Subsystem {

    int getPriority();

    List<ModelNode> getList();
}
