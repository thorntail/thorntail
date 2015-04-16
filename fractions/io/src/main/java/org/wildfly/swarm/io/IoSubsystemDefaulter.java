package org.wildfly.swarm.io;

import org.wildfly.swarm.container.SimpleSubsystemDefaulter;

/**
 * @author Bob McWhirter
 */
public class IoSubsystemDefaulter extends SimpleSubsystemDefaulter<IoSubsystem> {
    public IoSubsystemDefaulter() {
        super(IoSubsystem.class);
    }

}
