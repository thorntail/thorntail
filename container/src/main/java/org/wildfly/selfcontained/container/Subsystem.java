package org.wildfly.selfcontained.container;

import org.jboss.dmr.ModelNode;

import java.util.List;

/**
 * @author Bob McWhirter
 */
public interface Subsystem {

    List<ModelNode> getList();
}
