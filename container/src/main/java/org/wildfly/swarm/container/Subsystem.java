package org.wildfly.swarm.container;

import org.jboss.dmr.ModelNode;

import java.util.List;

/**
 * @author Bob McWhirter
 */
public interface Subsystem {

    int getPriority();
    String getName();

    List<ModelNode> getList();
}
