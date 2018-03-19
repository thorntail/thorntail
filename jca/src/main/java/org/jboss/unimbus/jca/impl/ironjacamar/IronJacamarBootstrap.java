package org.jboss.unimbus.jca.impl.ironjacamar;

import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

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
        BootstrapContextCoordinator.getInstance().setDefaultBootstrapContext(this.bootstrapContext);
    }

    @PreDestroy
    void tearDown() {
        WorkManagerCoordinator.getInstance().setDefaultWorkManager(null);
        BootstrapContextCoordinator contextCoordinator = BootstrapContextCoordinator.getInstance();
        contextCoordinator.setDefaultBootstrapContext(null);

        getActiveContextIds(contextCoordinator)
                .forEach(contextCoordinator::removeBootstrapContext);
    }

    @SuppressWarnings("unchecked")
    private Collection<String> getActiveContextIds(BootstrapContextCoordinator contextCoordinator) {
        try {
            Field activeContextsField = BootstrapContextCoordinator.class.getDeclaredField("activeBootstrapContexts");
            activeContextsField.setAccessible(true);
            Map<String, CloneableBootstrapContext> contexts =
                    (Map<String, CloneableBootstrapContext>) activeContextsField.get(contextCoordinator);
            return new ArrayList<>(contexts.keySet());
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new RuntimeException("Unable to get active bootstrap contexts", e);
        }
    }

    @Inject
    WorkManager workManager;

    @Inject
    CloneableBootstrapContext bootstrapContext;
}
