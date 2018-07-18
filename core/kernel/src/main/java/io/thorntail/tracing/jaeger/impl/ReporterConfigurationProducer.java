package io.thorntail.tracing.jaeger.impl;

import io.jaegertracing.internal.reporters.RemoteReporter;
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
public class ReporterConfigurationProducer {

    @Produces
    @Singleton
    Configuration.ReporterConfiguration reporterConfiguration() {
        return new Configuration.ReporterConfiguration()
            .withMaxQueueSize(this.queueSize.orElse(RemoteReporter.DEFAULT_MAX_QUEUE_SIZE))
            .withFlushInterval(this.flushInterval.orElse(RemoteReporter.DEFAULT_FLUSH_INTERVAL_MS))
            .withLogSpans(this.logSpans.orElse(false));
    }

    @Inject
    @ConfigProperty(name="jaeger.reporter.flush-interval")
    Optional<Integer> flushInterval;

    @Inject
    @ConfigProperty(name="jaeger.reporter.log-spans")
    Optional<Boolean> logSpans;

    @Inject
    @ConfigProperty(name="jaeger.reporter.max-queue-size")
    Optional<Integer> queueSize;
}
