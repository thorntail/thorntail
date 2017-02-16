package org.wildfly.swarm.opentracing.hawkular.jaxrs;

import org.hawkular.apm.api.services.TracePublisher;
import org.hawkular.apm.client.api.recorder.BatchTraceRecorder;
import org.hawkular.apm.client.api.recorder.LoggingRecorder;
import org.hawkular.apm.client.api.recorder.TraceRecorder;
import org.hawkular.apm.client.api.sampler.PercentageSampler;
import org.hawkular.apm.client.opentracing.APMTracer;
import org.hawkular.apm.client.opentracing.DeploymentMetaData;
import org.hawkular.apm.trace.publisher.rest.client.TracePublisherRESTClient;
import org.wildfly.swarm.spi.api.Fraction;
import org.wildfly.swarm.spi.api.annotations.DeploymentModule;
import org.wildfly.swarm.spi.api.annotations.DeploymentModules;

import io.opentracing.contrib.global.GlobalTracer;
import io.opentracing.contrib.jaxrs2.server.ServerTracingDynamicFeature;

/**
 * OpenTracing JAX-RS Hawkular fraction. This fraction traces all
 * server requests using OpenTracing API with Hawkular APM implementation.
 *
 * <p>Underneath It uses
 * <a href="http://www.github.com/opentracing-contrib/java-jaxrs">www.github.com/opentracing-contrib/java-jaxrs</a>
 * to do tracing. Therefore all tracing features provided by this library should be available.
 *
 * <p> Tracer instance in REST handlers can be accessed via {@linkplain GlobalTracer#get()}
 *
 * <p>Initialization:
 * {@code
 *  OpenTracingHawkularFraction openTracingHawkularFraction = new OpenTracingHawkularFraction();
 *
 *  openTracingHawkularFraction.tracerBuilder()
 *      .withServiceName("wildfly-swarm")
 *      .withBatchRecorderBuilder(new OpenTracingHawkularFraction.TraceRecorderBuilder()
 *      .withHttpRecorder("jdoe", "password", "http://localhost:8180"))
 *      .withSampleRate(100);
 *
 *  container.fraction(openTracingHawkularFraction);
 * }
 * @author Pavol Loffay
 */
@DeploymentModules({
        @DeploymentModule(name = "io.opentracing.hawkular"),
        @DeploymentModule(name = "org.wildfly.swarm.opentracing.hawkular.jaxrs", slot = "main")
})
public class OpenTracingHawkularFraction implements Fraction<OpenTracingHawkularFraction> {

    private APMJaxRsTracingBuilder apmJaxRsTracingBuilder;

    public OpenTracingHawkularFraction() {
        this.apmJaxRsTracingBuilder = new APMJaxRsTracingBuilder();
    }

    @Override
    public OpenTracingHawkularFraction applyDefaults() {
        return this;
    }

    public APMJaxRsTracingBuilder apmJaxRsTracingBuilder() {
        return apmJaxRsTracingBuilder;
    }

    public ServerTracingDynamicFeature.Builder getJaxrsTraceBuilder() {
        return apmJaxRsTracingBuilder.build();
    }

    public class APMJaxRsTracingBuilder {
        private int percentageSampling = 100;
        private String serviceName;
        private String buildStamp;
        private boolean consoleRecorder;
        private TraceRecorderBuilder batchTraceRecorderBuilder;

        public APMJaxRsTracingBuilder withSampleRate(int percentage) {
            this.percentageSampling = percentage;
            return this;
        }

        public APMJaxRsTracingBuilder withServiceName(String serviceName) {
            this.serviceName = serviceName;
            return this;
        }

        public APMJaxRsTracingBuilder withBuilStamp(String buildStamp) {
            this.buildStamp = buildStamp;
            return this;
        }

        public APMJaxRsTracingBuilder withConsoleRecorder(boolean consoleRecorder) {
            this.consoleRecorder = consoleRecorder;
            return this;
        }

        public APMJaxRsTracingBuilder withBatchRecorderBuilder(TraceRecorderBuilder builder) {
            this.batchTraceRecorderBuilder = builder;
            return this;
        }

        private ServerTracingDynamicFeature.Builder build() {
            TraceRecorder traceRecorder = consoleRecorder ? new LoggingRecorder() :
                    batchTraceRecorderBuilder != null ?
                            this.batchTraceRecorderBuilder.build() : new BatchTraceRecorder();

            APMTracer apmTracer = new APMTracer(traceRecorder, PercentageSampler.withPercentage(percentageSampling),
                    new DeploymentMetaData(serviceName, buildStamp));

            GlobalTracer.register(apmTracer);

            return ServerTracingDynamicFeature.Builder
                    .traceAll(apmTracer);
        }
    }

    public static class TraceRecorderBuilder extends BatchTraceRecorder.BatchTraceRecorderBuilder {

        public TraceRecorderBuilder withHttpRecorder(String userName, String password, String url) {
            super.withTracePublisher(new TracePublisherRESTClient(userName, password, url));
            return this;
        }

        /**
         * Do not use this method instead use {@link #withHttpRecorder(String, String, String)}.
         *
         * @param tracePublisher trace publisher
         * @return builder
         */
        @Deprecated
        @Override
        public BatchTraceRecorder.BatchTraceRecorderBuilder withTracePublisher(TracePublisher tracePublisher) {
            throw new IllegalArgumentException("Please use #withHttpRecoreder");
        }

    }
}

