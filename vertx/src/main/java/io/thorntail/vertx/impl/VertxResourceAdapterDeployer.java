package io.thorntail.vertx.impl;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import io.thorntail.jca.ResourceAdapterDeployment;
import io.thorntail.jca.ResourceAdapterDeploymentFactory;
import io.thorntail.jca.ResourceAdapterDeployments;
import io.thorntail.jca.impl.Util;
import org.jboss.jca.common.api.metadata.spec.ConfigProperty;
import org.jboss.jca.common.api.metadata.spec.ResourceAdapter;
import io.thorntail.events.LifecycleEvent;

/**
 * Created by bob on 2/9/18.
 */
@ApplicationScoped
public class VertxResourceAdapterDeployer {

    void init(@Observes LifecycleEvent.Scan event) throws Exception {
        ResourceAdapterDeployment deployment = this.factory.create("vertx", "META-INF/vertx-ra.xml");
        if (deployment == null) {
            return;
        }

        ResourceAdapter ra = deployment.getConnector().getResourceadapter();
        if (ra != null) {
            List<ConfigProperty> properties = ra.getConfigProperties();
            ConfigProperty clusterHost = find(properties, "clusterHost");
            setProperty(properties, clusterHost, this.config.getClusterHost());
            ConfigProperty clusterPort = find(properties, "clusterPort");
            setProperty(properties, clusterPort, "" + this.config.getClusterPort());
        }
        this.deployments.addDeployment(deployment);
    }

    ConfigProperty find(List<ConfigProperty> properties, String name) {
        return properties.stream()
                .filter(e -> e.getConfigPropertyName().getValue().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    void setProperty(List<ConfigProperty> properties, ConfigProperty original, String newValue) {
        ConfigProperty replacement = Util.duplicateProperty(original, newValue);
        properties.remove(original);
        properties.add(replacement);
    }

    @Inject
    ResourceAdapterDeployments deployments;

    @Inject
    ResourceAdapterDeploymentFactory factory;

    @Inject
    VertxConfiguration config;


}
