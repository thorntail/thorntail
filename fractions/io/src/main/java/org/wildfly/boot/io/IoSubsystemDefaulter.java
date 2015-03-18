package org.wildfly.boot.io;

import org.wildfly.boot.container.SimpleSubsystemDefaulter;

/**
 * @author Bob McWhirter
 */
public class IoSubsystemDefaulter extends SimpleSubsystemDefaulter<IoSubsystem> {
    public IoSubsystemDefaulter() {
        super(IoSubsystem.class);
    }

}
