package org.wildfly.swarm.request.controller;

import org.wildfly.swarm.container.SimpleSubsystemDefaulter;

/**
 * @author Bob McWhirter
 */
public class RequestControllerSubsystemDefaulter extends SimpleSubsystemDefaulter<RequestControllerSubsystem> {

    public RequestControllerSubsystemDefaulter() {
        super( RequestControllerSubsystem.class);
    }
}
