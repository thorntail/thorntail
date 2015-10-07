package org.wildfly.swarm.ee;

import org.wildfly.swarm.config.ee.Ee;
import org.wildfly.swarm.config.ee.subsystem.contextService.ContextService;
import org.wildfly.swarm.config.ee.subsystem.managedExecutorService.ManagedExecutorService;
import org.wildfly.swarm.config.ee.subsystem.managedScheduledExecutorService.ManagedScheduledExecutorService;
import org.wildfly.swarm.config.ee.subsystem.managedThreadFactory.ManagedThreadFactory;
import org.wildfly.swarm.config.ee.subsystem.service.DefaultBindings;
import org.wildfly.swarm.container.Container;
import org.wildfly.swarm.container.Fraction;

/**
 * @author Bob McWhirter
 */
public class EEFraction extends Ee<EEFraction> implements Fraction {

    public static final String CONCURRENCY_CONTEXT_DEFAULT = "java:jboss/ee/concurrency/context/default";
    public static final String CONCURRENCY_FACTORY_DEFAULT = "java:jboss/ee/concurrency/factory/default";
    public static final String CONCURRENCY_EXECUTOR_DEFAULT = "java:jboss/ee/concurrency/executor/default";
    public static final String CONCURRENCY_SCHEDULER_DEFAULT = "java:jboss/ee/concurrency/scheduler/default";
    public static final String DEFAULT_KEY = "default";

    public static EEFraction createDefaultFraction() {
        EEFraction fraction = new EEFraction();
        fraction.specDescriptorPropertyReplacement(false)
                .contextService(new ContextService(DEFAULT_KEY)
                        .jndiName(CONCURRENCY_CONTEXT_DEFAULT)
                        .useTransactionSetupProvider(false))
                .managedThreadFactory(new ManagedThreadFactory(DEFAULT_KEY)
                        .jndiName(CONCURRENCY_FACTORY_DEFAULT)
                        .contextService(DEFAULT_KEY))
                .managedExecutorService(new ManagedExecutorService(DEFAULT_KEY)
                        .jndiName(CONCURRENCY_EXECUTOR_DEFAULT)
                        .contextService(DEFAULT_KEY)
                        .hungTaskThreshold(60000L)
                        .coreThreads(5)
                        .maxThreads(25)
                        .keepaliveTime(5000L))
                .managedScheduledExecutorService(new ManagedScheduledExecutorService(DEFAULT_KEY)
                        .jndiName(CONCURRENCY_SCHEDULER_DEFAULT)
                        .contextService(DEFAULT_KEY)
                        .hungTaskThreshold(60000L)
                        .coreThreads(5)
                        .keepaliveTime(3000L));

        return fraction;
    }

    @Override
    public void postInitialize(Container.PostInitContext initContext) {
        if ( initContext.hasFraction( "Messaging" )) {
            if ( this.defaultBindings() == null ) {
                this.defaultBindings( new DefaultBindings() );
            }
            this.defaultBindings()
                    .jmsConnectionFactory( "java:jboss/DefaultJMSConnectionFactory" );
        }
    }
}
