package io.thorntail.tracing.jaeger.impl;

import io.jaegertracing.Configuration;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Singleton;

import io.thorntail.condition.annotation.RequiredClassPresent;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * Created by bob on 2/22/18.
 */
@ApplicationScoped
@RequiredClassPresent("io.jaegertracing.Configuration")
public class ConfigurationProducer {

    @Produces
    @Singleton
    Configuration configuration() {
        Configuration config = new Configuration(this.serviceName)
            .withSampler(this.samplerConfiguration)
            .withReporter(this.reporterConfiguration);
        return config;
    }

    void dispose(@Disposes Configuration config) {
        config.closeTracer();
    }

    @Inject
    @ConfigProperty(name = "jaeger.service-name")
    String serviceName;

    @Inject
    Configuration.SamplerConfiguration samplerConfiguration;

    @Inject
    Configuration.ReporterConfiguration reporterConfiguration;
}
