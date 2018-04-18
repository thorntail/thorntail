package io.thorntail.jca.impl.ironjacamar;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import org.jboss.jca.core.api.workmanager.WorkManager;
import org.jboss.jca.core.spi.security.SecurityIntegration;
import org.jboss.jca.core.workmanager.WorkManagerImpl;

import org.jboss.threads.JBossExecutors;

/**
 * Created by bob on 2/8/18.
 */
@ApplicationScoped
public class WorkManagerProducer {

    @Produces
    @ApplicationScoped
    WorkManager workManager() {
        WorkManagerImpl workManager = new WorkManagerImpl();
        workManager.setName("default");
        workManager.setId("default");
        workManager.setSecurityIntegration(this.securityIntegration);
        workManager.setShortRunningThreadPool(JBossExecutors.blockingDirectExecutor());
        workManager.setLongRunningThreadPool(JBossExecutors.blockingDirectExecutor());
        return workManager;
    }

    void shutdown(@Disposes WorkManager wm) {
        wm.shutdown();
    }

    @Inject
    private SecurityIntegration securityIntegration;
}
