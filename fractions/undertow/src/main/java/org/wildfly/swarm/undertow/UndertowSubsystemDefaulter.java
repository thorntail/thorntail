package org.wildfly.swarm.undertow;

import org.wildfly.swarm.container.SimpleSubsystemDefaulter;

/**
 * @author Bob McWhirter
 */
public class UndertowSubsystemDefaulter extends SimpleSubsystemDefaulter<UndertowSubsystem> {
    public UndertowSubsystemDefaulter() {
        super(UndertowSubsystem.class);
    }

}
