package org.wildfly.swarm.netflix.hystrix.runtime;

import javax.inject.Singleton;

import org.wildfly.swarm.netflix.archaius.runtime.ArchaiusLinkage;

/**
 * @author Bob McWhirter
 */
@Singleton
public class HystrixArchaiusLinkage extends ArchaiusLinkage {

    public HystrixArchaiusLinkage() {
        super("hystrix");
    }

}
