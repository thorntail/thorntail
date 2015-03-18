package org.wildfly.boot.jaxrs;

import org.wildfly.boot.container.SimpleSubsystemDefaulter;

/**
 * @author Bob McWhirter
 */
public class WeldSubsystemDefaulter extends SimpleSubsystemDefaulter<WeldSubsystem> {

    public WeldSubsystemDefaulter() {
        super(WeldSubsystem.class);
    }

}
