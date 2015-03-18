package org.wildfly.boot.ee;

import org.wildfly.boot.container.SimpleSubsystemDefaulter;

/**
 * @author Bob McWhirter
 */
public class EeSubsystemDefaulter extends SimpleSubsystemDefaulter<EeSubsystem> {

    public EeSubsystemDefaulter() {
        super(EeSubsystem.class);
    }

}
