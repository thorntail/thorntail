package org.wildfly.swarm.request.controller;

import org.wildfly.swarm.container.SimpleFractionDefaulter;

/**
 * @author Bob McWhirter
 */
public class RequestControllerFractionDefaulter extends SimpleFractionDefaulter<RequestControllerFraction> {

    public RequestControllerFractionDefaulter() {
        super( RequestControllerFraction.class);
    }
}
