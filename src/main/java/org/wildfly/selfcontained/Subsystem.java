package org.wildfly.selfcontained;

import org.jboss.dmr.ModelNode;

import java.util.List;

/**
 * @author Bob McWhirter
 */
public interface Subsystem {

    List<ModelNode> getList();
}
