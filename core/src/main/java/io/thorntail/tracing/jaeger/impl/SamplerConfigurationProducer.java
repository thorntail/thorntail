package io.thorntail.tracing.jaeger.impl;

import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Singleton;

import com.uber.jaeger.Configuration;
import com.uber.jaeger.samplers.RemoteControlledSampler;
import io.thorntail.condition.annotation.RequiredClassPresent;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * Created by bob on 2/22/18.
 */
@ApplicationScoped
@RequiredClassPresent("com.uber.jaeger.Configuration")
public class SamplerConfigurationProducer {

    @Produces
    @Singleton
    Configuration.SamplerConfiguration samplerConfiguration() {
        Configuration.SamplerConfiguration config = new Configuration.SamplerConfiguration(
                this.type.orElse(RemoteControlledSampler.TYPE),
                this.param.orElse(Configuration.DEFAULT_SAMPLING_PROBABILITY),
                this.managerHostPort.orElse(null)
        );
        return config;
    }

    @Inject
    @ConfigProperty(name="jaeger.sampler.type")
    Optional<String> type;

    @Inject
    @ConfigProperty(name="jaeger.sampler.param")
    Optional<Double> param;

    @Inject
    @ConfigProperty(name="jaeger.sampler.manager.host-port")
    Optional<String> managerHostPort;
}
