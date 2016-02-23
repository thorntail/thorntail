package org.wildfly.swarm.topology.openshift.runtime;

import org.jboss.msc.service.ServiceActivator;
import org.wildfly.swarm.container.runtime.AbstractServerConfiguration;
import org.wildfly.swarm.topology.openshift.OpenShiftTopologyFraction;

import java.util.ArrayList;
import java.util.List;

public class OpenShiftTopologyConfiguration extends AbstractServerConfiguration<OpenShiftTopologyFraction> {

    public OpenShiftTopologyConfiguration() {
        super(OpenShiftTopologyFraction.class);
    }

    @Override
    public List<ServiceActivator> getServiceActivators(OpenShiftTopologyFraction fraction) {
        List<ServiceActivator> activators = new ArrayList<>();
        activators.add(new OpenShiftTopologyConnectorActivator());
        return activators;
    }

}
