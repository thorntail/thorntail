package org.wildfly.boot.naming;

import org.wildfly.boot.container.SimpleSubsystemDefaulter;

/**
 * @author Bob McWhirter
 */
public class NamingSubsystemDefaulter extends SimpleSubsystemDefaulter<NamingSubsystem> {
    public NamingSubsystemDefaulter() {
        super(NamingSubsystem.class);
    }

}
