package org.wildfly.swarm.examples.messaging;

import org.jboss.msc.service.ServiceActivator;
import org.jboss.msc.service.ServiceActivatorContext;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.ServiceRegistryException;
import org.jboss.msc.service.ServiceTarget;

/**
 * @author Bob McWhirter
 */
public class MyServiceActivator implements ServiceActivator {
    public void activate(ServiceActivatorContext context) throws ServiceRegistryException {

        System.err.println("activating services");
        ServiceTarget target = context.getServiceTarget();

        target.addService(ServiceName.of("my", "service", "1"), new MyService("/jms/topic/my-topic"))
                .install();
    }
}
