package org.wildfly.swarm.container.runtime.cli;

import java.util.Arrays;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.jboss.msc.service.ServiceActivator;
import org.jboss.msc.service.ServiceActivatorContext;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.ServiceRegistryException;
import org.jboss.msc.service.ValueService;
import org.jboss.msc.value.ImmediateValue;

/**
 * @author Bob McWhirter
 */
@Singleton
public class CommandLineArgsServiceActivator implements ServiceActivator {

    @Inject
    @CommandLineArgs String[] args;

    @Override
    public void activate(ServiceActivatorContext context) throws ServiceRegistryException {
        System.err.println( "install MSC args for " + Arrays.asList( this.args ) );
        context.getServiceTarget().addService(ServiceName.of("wildfly", "swarm", "main-args"), new ValueService<>(new ImmediateValue<>(this.args)))
                .install();
    }
}
