package org.wildfly.swarm.jca.runtime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.PathElement;
import org.jboss.dmr.ModelNode;
import org.wildfly.apigen.invocation.Marshaller;
import org.wildfly.swarm.config.jca.subsystem.archiveValidation.ArchiveValidation;
import org.wildfly.swarm.config.jca.subsystem.beanValidation.BeanValidation;
import org.wildfly.swarm.config.jca.subsystem.bootstrapContext.BootstrapContext;
import org.wildfly.swarm.config.jca.subsystem.cachedConnectionManager.CachedConnectionManager;
import org.wildfly.swarm.config.jca.subsystem.workmanager.Workmanager;
import org.wildfly.swarm.config.jca.subsystem.workmanager.longRunningThreads.LongRunningThreads;
import org.wildfly.swarm.config.jca.subsystem.workmanager.shortRunningThreads.ShortRunningThreads;
import org.wildfly.swarm.container.runtime.AbstractServerConfiguration;
import org.wildfly.swarm.jca.JCAFraction;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ADD;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP_ADDR;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.SUBSYSTEM;

/**
 * @author Bob McWhirter
 * @author Lance Ball
 */
public class JCAConfiguration extends AbstractServerConfiguration<JCAFraction> {

    public JCAConfiguration() {
        super(JCAFraction.class);
    }

    @Override
    public JCAFraction defaultFraction() {
        Map keepAlive = new HashMap<>();
        keepAlive.put("time", "10");
        keepAlive.put("unit", "SECONDS");
        JCAFraction fraction = new JCAFraction();
        fraction.archiveValidation(new ArchiveValidation()
                .enabled(true)
                .failOnError(true)
                .failOnWarn(true))
                .beanValidation(new BeanValidation()
                        .enabled(true))
                .workmanager(new Workmanager("default")
                        .name("default")
                        .shortRunningThreads(new ShortRunningThreads("default")
                                .coreThreads(50)
                                .queueLength(50)
                                .maxThreads(50)
                                .keepaliveTime(keepAlive))
                        .longRunningThreads(new LongRunningThreads("default")
                                .coreThreads(50)
                                .queueLength(50)
                                .maxThreads(50)
                                .keepaliveTime(keepAlive)))
                .bootstrapContext(new BootstrapContext("default")
                        .workmanager("default")
                        .name("default"))
                .cachedConnectionManager(new CachedConnectionManager().install(true));
        return fraction;
    }

    @Override
    public List<ModelNode> getList(JCAFraction fraction) {
        try {
            return Marshaller.marshal(fraction);
        } catch (Exception e) {
            System.err.println("Cannot configure JCA subsystem. " + e);
        }
        return new ArrayList<>();
    }
}
