package org.wildfly.swarm.ee.runtime;

import java.util.ArrayList;
import java.util.List;

import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.PathElement;
import org.jboss.dmr.ModelNode;
import org.wildfly.apigen.invocation.Marshaller;
import org.wildfly.swarm.config.ee.subsystem.contextService.ContextService;
import org.wildfly.swarm.config.ee.subsystem.managedExecutorService.ManagedExecutorService;
import org.wildfly.swarm.config.ee.subsystem.managedScheduledExecutorService.ManagedScheduledExecutorService;
import org.wildfly.swarm.config.ee.subsystem.managedThreadFactory.ManagedThreadFactory;
import org.wildfly.swarm.config.ee.subsystem.service.DefaultBindings;
import org.wildfly.swarm.container.runtime.AbstractServerConfiguration;
import org.wildfly.swarm.ee.EEFraction;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ADD;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.EXTENSION;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP_ADDR;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.SUBSYSTEM;

/**
 * @author Bob McWhirter
 * @author Lance Ball
 */
public class EEConfiguration extends AbstractServerConfiguration<EEFraction> {

    public EEConfiguration() {
        super(EEFraction.class);
    }

    @Override
    public EEFraction defaultFraction() {

        EEFraction fraction = new EEFraction();
        fraction.specDescriptorPropertyReplacement(false)
                .contextService(new ContextService("default")
                    .jndiName("java:jboss/ee/concurrency/context/default")
                    .useTransactionSetupProvider(false))
                .managedThreadFactory(new ManagedThreadFactory("default")
                    .jndiName("java:jboss/ee/concurrency/factory/default")
                    .contextService("default"))
                .managedExecutorService(new ManagedExecutorService("default")
                    .jndiName("java:jboss/ee/concurrency/executor/default")
                    .contextService("default")
                    .hungTaskThreshold(60000L)
                    .coreThreads(5)
                    .maxThreads(25)
                    .keepaliveTime(5000L))
                .managedScheduledExecutorService(new ManagedScheduledExecutorService("default")
                    .jndiName("java:jboss/ee/concurrency/scheduler/default")
                    .contextService("default")
                    .hungTaskThreshold(60000L)
                    .coreThreads(5)
                    .keepaliveTime(3000L));

// TODO: These were commented out in the original ModelNode implementation. Do we want to set default bindings or not?
//        fraction.defaultBindings(new DefaultBindings()
//            .contextService("java:jboss/ee/concurrency/context/default"))
//            .managedExecutorService(new ManagedExecutorService("java:jboss/ee/concurrency/executor/default"))
//            .managedScheduledExecutorService(new ManagedScheduledExecutorService("java:jboss/ee/concurrency/scheduler/default"))
//            .managedThreadFactory(new ManagedThreadFactory("java:jboss/ee/concurrency/factory/default"));

        return fraction;
    }

    @Override
    public List<ModelNode> getList(EEFraction fraction) {

        List<ModelNode> list = new ArrayList<>();
        PathAddress address = PathAddress.pathAddress(PathElement.pathElement(SUBSYSTEM, "ee"));

        ModelNode node = new ModelNode();
        node.get(OP_ADDR).set(EXTENSION, "org.jboss.as.ee");
        node.get(OP).set(ADD);
        list.add(node);

        try {
            list.addAll(Marshaller.marshal(fraction));
        } catch (Exception e) {
            System.err.println("Cannot configure EE subsystem " + e);
        }
        return list;

    }
}
