package org.wildfly.boot.web;

import org.wildfly.boot.container.SimpleSubsystemDefaulter;

/**
 * @author Bob McWhirter
 */
public class EeSubsystemDefaulter extends SimpleSubsystemDefaulter<EeSubsystem> {
    
    public EeSubsystemDefaulter() {
        super(EeSubsystem.class);
    }
    
}
