package org.jboss.unimbus.jca.ironjacamar;

import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.BeforeDestroyed;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.jboss.jca.core.api.workmanager.WorkManager;
import org.jboss.jca.core.api.bootstrap.CloneableBootstrapContext;
import org.jboss.jca.core.bootstrapcontext.BootstrapContextCoordinator;
import org.jboss.jca.core.workmanager.WorkManagerCoordinator;
import org.jboss.unimbus.events.LifecycleEvent;

/**
 * Created by bob on 2/8/18.
 */
@ApplicationScoped
public class IronJacamarBootstrap {

    void setup(@Observes LifecycleEvent.Bootstrap event) {
        WorkManagerCoordinator.getInstance().setDefaultWorkManager(this.workManager);
        BootstrapContextCoordinator.getInstance().setDefaultBootstrapContext( this.bootstrapContext );
    }

    @PreDestroy
    void tearDown() {
        WorkManagerCoordinator.getInstance().setDefaultWorkManager(null);
        BootstrapContextCoordinator.getInstance().setDefaultBootstrapContext(null);
    }

    @Inject
    WorkManager workManager;

    @Inject
    CloneableBootstrapContext bootstrapContext;
}
