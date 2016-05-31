package org.wildfly.swarm.request.controller;

import org.wildfly.swarm.ContainerFactory;
import org.wildfly.swarm.container.Container;

public class RequestControllerContainerFactory implements ContainerFactory {

    @Override
    public Container newContainer(String... args) throws Exception {
        return new Container().fraction(RequestControllerFraction.createDefaultFraction());
    }
}
