package org.wildfly.swarm.container.runtime.xmlconfig;

import java.util.List;

import org.jboss.dmr.ModelNode;

/**
 * @author Heiko Braun
 * @since 14/09/16
 */
public interface BootstrapConfiguration {
    List<ModelNode> get();
}
