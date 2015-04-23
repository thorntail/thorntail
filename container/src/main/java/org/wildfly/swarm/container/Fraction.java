package org.wildfly.swarm.container;

import org.jboss.dmr.ModelNode;

import java.util.List;

/**
 * @author Bob McWhirter
 */
public interface Fraction {

    int getPriority();
    String getName();

    List<ModelNode> getList();
}
