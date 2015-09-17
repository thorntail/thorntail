package org.wildfly.swarm.ejb.runtime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.PathElement;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.ValueExpression;
import org.wildfly.apigen.invocation.Marshaller;
import org.wildfly.swarm.config.ejb3.subsystem.cache.Cache;
import org.wildfly.swarm.config.ejb3.subsystem.service.Async;
import org.wildfly.swarm.config.ejb3.subsystem.service.TimerService;
import org.wildfly.swarm.config.ejb3.subsystem.service.fileDataStore.FileDataStore;
import org.wildfly.swarm.config.ejb3.subsystem.strictMaxBeanInstancePool.StrictMaxBeanInstancePool;
import org.wildfly.swarm.config.ejb3.subsystem.threadPool.ThreadPool;
import org.wildfly.swarm.container.runtime.AbstractServerConfiguration;
import org.wildfly.swarm.ejb.EJBFraction;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ADD;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.EXTENSION;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP_ADDR;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.SUBSYSTEM;

/**
 * @author Ken Finnigan
 */
public class EJBConfiguration extends AbstractServerConfiguration<EJBFraction> {

    private PathAddress address = PathAddress.pathAddress(PathElement.pathElement(SUBSYSTEM, "ejb3"));

    public EJBConfiguration() {
        super(EJBFraction.class);
    }

    @Override
    public EJBFraction defaultFraction() {

        Map threadPoolSettings = new HashMap<>();
        threadPoolSettings.put("time", "100");
        threadPoolSettings.put("unit", "MILLISECONDS");

        EJBFraction fraction = new EJBFraction();
        fraction.defaultStatefulBeanAccessTimeout(5000L)
                .defaultSingletonBeanAccessTimeout(5000L)
                .defaultSfsbCache("simple")
                .defaultSecurityDomain("other")
                .defaultMissingMethodPermissionsDenyAccess(true)
                .logSystemExceptions(true)
                .defaultResourceAdapterName(
                        new ValueExpression("${ejb.resource-adapter-name:activemq-ra.rar}").resolveString())
                .strictMaxBeanInstancePool(new StrictMaxBeanInstancePool("slsb-strict-max-pool")
                        .maxPoolSize(20)
                        .timeout(5L)
                        .timeoutUnit("MINUTES"))
                .strictMaxBeanInstancePool(new StrictMaxBeanInstancePool("mdb-strict-max-pool")
                        .maxPoolSize(20)
                        .timeout(5L)
                        .timeoutUnit("MINUTES"))
                .cache(new Cache("simple"))
                .async(new Async().threadPoolName("default"))
                        .timerService(new TimerService()
                        .threadPoolName("default")
                        .defaultDataStore("default-file-store")
                        .fileDataStore(new FileDataStore("default-file-store")
                                .path("timer-service-data")
                                .relativeTo("jboss.server.data.dir")))
                        .threadPool(new ThreadPool("default")
                                .maxThreads(10)
                                .keepaliveTime(threadPoolSettings));

        return fraction;
    }

    @Override
    public List<ModelNode> getList(EJBFraction fraction) {
        List<ModelNode> list = new ArrayList<>();

        ModelNode node = new ModelNode();
        node.get(OP_ADDR).set(EXTENSION, "org.jboss.as.ejb3");
        node.get(OP).set(ADD);
        list.add(node);

        try {
            list.addAll(Marshaller.marshal(fraction));
        } catch (Exception e) {
            System.err.println("Cannot configure EJB subsystem. " + e);
        }
        return list;
    }
}
