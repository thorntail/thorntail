package io.thorntail.tracing.jaeger.impl;

import io.jaegertracing.internal.samplers.ProbabilisticSampler;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Singleton;

import io.jaegertracing.Configuration;
import io.thorntail.condition.annotation.RequiredClassPresent;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * Created by bob on 2/22/18.
 */
@ApplicationScoped
@RequiredClassPresent("io.jaegertracing.Configuration")
public class SamplerConfigurationProducer {

    @Produces
    @Singleton
    Configuration.SamplerConfiguration samplerConfiguration() {
        return new Configuration.SamplerConfiguration()
            .withType(this.type.orElse(ProbabilisticSampler.TYPE))
            .withParam(this.param.orElse(ProbabilisticSampler.DEFAULT_SAMPLING_PROBABILITY));
    }

    @Inject
    @ConfigProperty(name="jaeger.sampler.type")
    Optional<String> type;

    @Inject
    @ConfigProperty(name="jaeger.sampler.param")
    Optional<Double> param;
}
