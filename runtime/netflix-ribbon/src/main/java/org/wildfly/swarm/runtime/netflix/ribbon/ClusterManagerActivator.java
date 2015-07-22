package org.wildfly.swarm.runtime.netflix.ribbon;

import org.jboss.as.network.SocketBinding;
import org.jboss.msc.service.*;
import org.wildfly.clustering.dispatcher.CommandDispatcherFactory;

/**
 * @author Bob McWhirter
 */
public class ClusterManagerActivator implements ServiceActivator {

    @Override
    public void activate(ServiceActivatorContext context) throws ServiceRegistryException {
        ServiceTarget target = context.getServiceTarget();

        ClusterManager manager = new ClusterManager();

        target.addService(ClusterManager.SERVICE_NAME, manager)
                .addDependency(ServiceName.parse("jboss.clustering.dispatcher.default"), CommandDispatcherFactory.class, manager.getCommandDispatcherFactoryInjector())
                .addDependency( ServiceName.parse( "org.wildfly.network.socket-binding.http" ), SocketBinding.class, manager.getSocketBindingInjector() )
                .install();

    }
}
