package org.wildfly.swarm.naming;

import org.wildfly.swarm.container.SimpleSubsystemDefaulter;

/**
 * @author Bob McWhirter
 */
public class NamingSubsystemDefaulter extends SimpleSubsystemDefaulter<NamingSubsystem> {
    public NamingSubsystemDefaulter() {
        super(NamingSubsystem.class);
    }

}
