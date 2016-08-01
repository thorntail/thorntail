package org.wildfly.swarm.container.runtime.internal.marshal;

import java.util.List;

import org.jboss.dmr.ModelNode;

/**
 * @author Bob McWhirter
 */
public interface ConfigurationMarshaller {

    List<ModelNode> marshal();
}
