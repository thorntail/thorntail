package org.jboss.unimbus.jca.ironjacamar;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import org.jboss.jca.core.api.bootstrap.CloneableBootstrapContext;
import org.jboss.jca.core.api.workmanager.WorkManager;
import org.jboss.jca.core.bootstrapcontext.BaseCloneableBootstrapContext;

/**
 * Created by bob on 2/8/18.
 */
@ApplicationScoped
public class BootstrapContextProducer {

    @Produces
    @ApplicationScoped
    CloneableBootstrapContext context() {
        BaseCloneableBootstrapContext context = new BaseCloneableBootstrapContext();
        context.setId( "default" );
        context.setName( "default" );
        context.setWorkManager( this.workManager);
        return context;
    }

    void shutdown(@Disposes CloneableBootstrapContext context) {
        context.shutdown();
    }

    @Inject
    WorkManager workManager;
}
