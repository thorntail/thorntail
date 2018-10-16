package io.thorntail.agroal.impl;

import io.agroal.api.AgroalDataSource;
import io.thorntail.agroal.AgroalPoolMetaData;
import io.thorntail.events.LifecycleEvent;

import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by bob on 2/8/18.
 */
@ApplicationScoped
public class DeploymentActivator {

    void init(@Observes LifecycleEvent.Initialize event) {
        for (AgroalPoolMetaData metaData : this.poolRegistry.getDatasources()) {
            try {
                this.dataSources.add(agroalProducer.deploy(metaData));
            } catch (Throwable t) {
                throw new RuntimeException(t);
            }
        }
    }

    @PreDestroy
    void stop() {
        for (AgroalDataSource dataSource : this.dataSources) {
            //pool is being closed before this point
            //dataSource.close();
        }
    }

    @Inject
    AgroalProducer agroalProducer;

    @Inject
    AgroalPoolRegistry poolRegistry;

    private List<AgroalDataSource> dataSources = new ArrayList<>();
}
