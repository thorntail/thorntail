package io.thorntail.vertx.impl;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * Created by bob on 2/12/18.
 */
@ApplicationScoped
public class VertxConfiguration {

    public String getClusterHost() {
        return this.clusterHost;
    }

    public int getClusterPort() {
        return this.clusterPort;
    }

    @Inject
    @ConfigProperty(name="vertx.cluster-host")
    String clusterHost;

    @Inject
    @ConfigProperty(name="vertx.cluster-port")
    int clusterPort;
}
