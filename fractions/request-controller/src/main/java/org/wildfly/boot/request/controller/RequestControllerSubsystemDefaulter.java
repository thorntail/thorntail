package org.wildfly.boot.request.controller;

import org.wildfly.boot.container.SimpleSubsystemDefaulter;

/**
 * @author Bob McWhirter
 */
public class RequestControllerSubsystemDefaulter extends SimpleSubsystemDefaulter<RequestControllerSubsystem> {

    public RequestControllerSubsystemDefaulter() {
        super( RequestControllerSubsystem.class);
    }
}
