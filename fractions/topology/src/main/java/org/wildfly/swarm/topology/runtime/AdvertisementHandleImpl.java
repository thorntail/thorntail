package org.wildfly.swarm.topology.runtime;

import org.jboss.msc.service.ServiceController;
import org.wildfly.swarm.topology.AdvertisementHandle;

/**
 * @author Bob McWhirter
 */
class AdvertisementHandleImpl implements AdvertisementHandle {

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
