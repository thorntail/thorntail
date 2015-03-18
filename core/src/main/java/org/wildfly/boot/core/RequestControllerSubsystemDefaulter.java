package org.wildfly.boot.core;

import org.wildfly.boot.container.AbstractSubsystemDefaulter;
import org.wildfly.boot.container.SimpleSubsystemDefaulter;

/**
 * @author Bob McWhirter
 */
public class RequestControllerSubsystemDefaulter extends SimpleSubsystemDefaulter<RequestControllerSubsystem> {

    public RequestControllerSubsystemDefaulter() {
        super( RequestControllerSubsystem.class);
    }
}
