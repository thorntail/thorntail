package org.jboss.unimbus.servlet.undertow.metrics;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import io.undertow.server.HttpHandler;
import org.eclipse.microprofile.metrics.MetricRegistry;
import org.eclipse.microprofile.metrics.annotation.RegistryType;
import org.jboss.unimbus.condition.IfClassPresent;

/**
 * Created by bob on 1/23/18.
 */
@ApplicationScoped
@IfClassPresent("org.eclipse.microprofile.metrics.MetricRegistry")
public class MetricsIntegration {

    public HttpHandler integrate(String deploymentName, HttpHandler next) {
        return new MetricsHandler(deploymentName, this.registry, next);
    }

    @Inject
    @RegistryType(type = MetricRegistry.Type.VENDOR)
    private MetricRegistry registry;
}
