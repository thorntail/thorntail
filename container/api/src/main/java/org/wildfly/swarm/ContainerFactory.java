package org.wildfly.swarm;

import org.wildfly.swarm.container.Container;

/**
 * @author Bob McWhirter
 */
public interface ContainerFactory {
    Container newContainer(String...args) throws Exception;
}
