package org.wildfly.swarm.topology.openshift.runtime;

import org.jboss.msc.service.ServiceActivator;
import org.jboss.msc.service.ServiceActivatorContext;
import org.jboss.msc.service.ServiceRegistryException;
import org.jboss.msc.service.ServiceTarget;
import org.wildfly.swarm.topology.runtime.TopologyConnector;
import org.wildfly.swarm.topology.runtime.TopologyManager;

public class OpenShiftTopologyConnectorActivator implements ServiceActivator {

    @Override
    public void activate(ServiceActivatorContext context) throws ServiceRegistryException {
        ServiceTarget target = context.getServiceTarget();

        OpenShiftTopologyConnector connector = new OpenShiftTopologyConnector();

        target.addService(TopologyConnector.SERVICE_NAME, connector)
                .addDependency(TopologyManager.SERVICE_NAME, TopologyManager.class, connector.getTopologyManagerInjector())
                .install();
    }
}
