package io.thorntail.vertx.impl;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import io.vertx.resourceadapter.impl.VertxPlatformConfiguration;

/**
 * Created by bob on 6/21/18.
 */
@ApplicationScoped
public class VertxPlatformConfigProducer {

    @Produces
    @ApplicationScoped
    VertxPlatformConfiguration vertxPlatformConfiguration() {
        VertxPlatformConfiguration platformConfig = new VertxPlatformConfiguration();
        platformConfig.setClustered(true);
        platformConfig.setClusterHost( this.config.getClusterHost() );
        platformConfig.setClusterPort( this.config.getClusterPort() );
        return platformConfig;
    }

    @Inject
    VertxConfiguration config;
}
