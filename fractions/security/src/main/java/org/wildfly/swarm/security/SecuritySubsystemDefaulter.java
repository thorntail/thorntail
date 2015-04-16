package org.wildfly.swarm.security;

import org.wildfly.swarm.container.SimpleSubsystemDefaulter;

/**
 * @author Bob McWhirter
 */
public class SecuritySubsystemDefaulter extends SimpleSubsystemDefaulter<SecuritySubsystem> {
    public SecuritySubsystemDefaulter() {
        super(SecuritySubsystem.class);
    }

}
