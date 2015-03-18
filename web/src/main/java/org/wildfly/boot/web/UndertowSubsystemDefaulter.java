package org.wildfly.boot.web;

import org.wildfly.boot.container.SimpleSubsystemDefaulter;

/**
 * @author Bob McWhirter
 */
public class UndertowSubsystemDefaulter extends SimpleSubsystemDefaulter<UndertowSubsystem> {
    public UndertowSubsystemDefaulter() {
        super(UndertowSubsystem.class);
    }

}
