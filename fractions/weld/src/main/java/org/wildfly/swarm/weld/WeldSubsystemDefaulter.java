package org.wildfly.swarm.weld;

import org.wildfly.swarm.container.SimpleSubsystemDefaulter;

/**
 * @author Bob McWhirter
 */
public class WeldSubsystemDefaulter extends SimpleSubsystemDefaulter<WeldSubsystem> {

    public WeldSubsystemDefaulter() {
        super(WeldSubsystem.class);
    }

}
