package org.wildfly.swarm.spi.api;

import javax.annotation.PostConstruct;

/**
 * @author Bob McWhirter
 */
public interface Customizer {

    @PostConstruct
    void customize();
}
