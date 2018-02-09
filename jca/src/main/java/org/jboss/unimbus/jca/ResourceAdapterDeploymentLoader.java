package org.jboss.unimbus.jca;

import java.util.List;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.unimbus.events.LifecycleEvent;
import org.jboss.unimbus.jca.ironjacamar.ResourceAdapterDeployment;

/**
 * Created by bob on 2/9/18.
 */
@ApplicationScoped
public class ResourceAdapterDeploymentLoader {

    private static final String META_INF = "META-INF/";

    void init(@Observes LifecycleEvent.Scan event) throws Exception {
        if (!this.raNames.isPresent()) {
            init(META_INF + "ra.xml");
            return;
        }

        for (String each : this.raNames.get()) {
            init(META_INF + each + "-ra.xml");
        }
    }

    void init(String path) throws Exception {
        ResourceAdapterDeployment deployment = this.factory.create(path);
        if (deployment != null) {
            this.deployments.addDeployment(deployment);
        }
    }

    @Inject
    ResourceAdapterDeploymentFactory factory;

    @Inject
    ResourceAdapterDeployments deployments;

    @Inject
    @ConfigProperty(name = "jca.resource-adapters")
    Optional<List<String>> raNames;
}
