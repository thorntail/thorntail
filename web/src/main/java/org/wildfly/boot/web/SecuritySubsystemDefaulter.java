package org.wildfly.boot.web;

import org.wildfly.boot.container.SimpleSubsystemDefaulter;

/**
 * @author Bob McWhirter
 */
public class SecuritySubsystemDefaulter extends SimpleSubsystemDefaulter<SecuritySubsystem> {
    public SecuritySubsystemDefaulter() {
        super(SecuritySubsystem.class);
    }

}
