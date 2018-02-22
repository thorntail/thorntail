package org.jboss.unimbus.opentracing.jaeger.impl;

import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import com.uber.jaeger.Configuration;
import com.uber.jaeger.samplers.RemoteControlledSampler;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.unimbus.condition.annotation.RequiredClassPresent;

/**
 * Created by bob on 2/22/18.
 */
@ApplicationScoped
@RequiredClassPresent("com.uber.jaeger.Configuration")
public class SamplerConfigurationProducer {

    @Produces
    @ApplicationScoped
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
    Optional<Number> param;

    @Inject
    @ConfigProperty(name="jaeger.sampler.manager.host-port")
    Optional<String> managerHostPort;
}
