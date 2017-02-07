package org.wildfly.swarm.netflix.hystrix.runtime;

import javax.enterprise.context.ApplicationScoped;

import org.wildfly.swarm.netflix.archaius.runtime.ArchaiusLinkage;

/**
 * @author Bob McWhirter
 */
@ApplicationScoped
public class HystrixArchaiusLinkage extends ArchaiusLinkage {

    public HystrixArchaiusLinkage() {
        super("hystrix");
    }

}
