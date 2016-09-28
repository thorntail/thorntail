package org.wildfly.swarm.topology.runtime;

import org.jboss.msc.service.ServiceController;
import org.wildfly.swarm.topology.AdvertisemetHandle;

/**
 * @author Bob McWhirter
 */
class AdvertisementHandleImpl implements AdvertisemetHandle {

    AdvertisementHandleImpl(ServiceController<?>...controllers) {
        this.controllers = controllers;
    }

    @Override
    public void unadvertise() {
        for (ServiceController<?> controller : this.controllers) {
            controller.setMode(ServiceController.Mode.REMOVE);
        }
    }

    private final ServiceController<?>[] controllers;

}
