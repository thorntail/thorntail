package org.wildfly.swarm.ee;

import org.wildfly.swarm.container.SimpleSubsystemDefaulter;

/**
 * @author Bob McWhirter
 */
public class EeSubsystemDefaulter extends SimpleSubsystemDefaulter<EeSubsystem> {

    public EeSubsystemDefaulter() {
        super(EeSubsystem.class);
    }

}
