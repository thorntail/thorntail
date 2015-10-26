package org.wildfly.swarm.jca;

import org.wildfly.swarm.config.JCA;
import org.wildfly.swarm.config.jca.ArchiveValidation;
import org.wildfly.swarm.config.jca.BeanValidation;
import org.wildfly.swarm.config.jca.BootstrapContext;
import org.wildfly.swarm.config.jca.CachedConnectionManager;
import org.wildfly.swarm.config.jca.LongRunningThreads;
import org.wildfly.swarm.config.jca.ShortRunningThreads;
import org.wildfly.swarm.config.jca.Workmanager;
import org.wildfly.swarm.container.Fraction;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Bob McWhirter
 */
public class JCAFraction extends JCA<JCAFraction> implements Fraction {

    private JCAFraction() {
    }

    public static JCAFraction createDefaultFraction() {
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
}
