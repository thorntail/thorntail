package org.jboss.unimbus.vertx.impl;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.jboss.jca.common.api.metadata.spec.ConfigProperty;
import org.jboss.jca.common.api.metadata.spec.ResourceAdapter;
import org.jboss.unimbus.events.LifecycleEvent;
import org.jboss.unimbus.jca.ResourceAdapterDeploymentFactory;
import org.jboss.unimbus.jca.ResourceAdapterDeployments;
import org.jboss.unimbus.jca.ResourceAdapterDeployment;

import static org.jboss.unimbus.jca.impl.Util.duplicateProperty;

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
        ConfigProperty replacement = duplicateProperty(original, newValue);
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
