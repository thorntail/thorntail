package org.wildfly.swarm.ejb;

import org.wildfly.swarm.config.Ejb3;
import org.wildfly.swarm.config.ejb3.AsyncService;
import org.wildfly.swarm.config.ejb3.Cache;
import org.wildfly.swarm.config.ejb3.StrictMaxBeanInstancePool;
import org.wildfly.swarm.config.ejb3.ThreadPool;
import org.wildfly.swarm.config.ejb3.TimerService;
import org.wildfly.swarm.config.ejb3.service.FileDataStore;
import org.wildfly.swarm.container.Fraction;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Ken Finnigan
 * @author Lance Ball
 */
public class EJBFraction extends Ejb3<EJBFraction> implements Fraction {

    protected EJBFraction() {

    }

    public static EJBFraction createDefaultFraction() {

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
                .defaultResourceAdapterName("${ejb.resource-adapter-name:activemq-ra.rar}")
                .strictMaxBeanInstancePool(new StrictMaxBeanInstancePool("slsb-strict-max-pool")
                        .maxPoolSize(20)
                        .timeout(5L)
                        .timeoutUnit("MINUTES"))
                .strictMaxBeanInstancePool(new StrictMaxBeanInstancePool("mdb-strict-max-pool")
                        .maxPoolSize(20)
                        .timeout(5L)
                        .timeoutUnit("MINUTES"))
                .cache(new Cache("simple"))
                .asyncService(new AsyncService().threadPoolName("default"))
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
}
