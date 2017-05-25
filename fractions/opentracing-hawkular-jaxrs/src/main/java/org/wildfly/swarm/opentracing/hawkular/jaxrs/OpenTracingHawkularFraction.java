package org.wildfly.swarm.opentracing.hawkular.jaxrs;

import java.util.function.Consumer;

import io.opentracing.contrib.global.GlobalTracer;
import org.wildfly.swarm.config.runtime.SubresourceInfo;
import org.wildfly.swarm.spi.api.Fraction;
import org.wildfly.swarm.spi.api.annotations.Configurable;
import org.wildfly.swarm.spi.api.annotations.DeploymentModule;
import org.wildfly.swarm.spi.api.annotations.DeploymentModules;

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
 * OpenTracingHawkularFraction openTracingHawkularFraction = new OpenTracingHawkularFraction();
 *
 * openTracingHawkularFraction.tracerBuilder()
 * .withServiceName("wildfly-swarm")
 * .withBatchRecorderBuilder(new OpenTracingHawkularFraction.NotTraceRecorder()
 * .withHttpRecorder("jdoe", "password", "http://localhost:8180"))
 * .withSampleRate(100);
 *
 * container.fraction(openTracingHawkularFraction);
 * }
 *
 * @author Pavol Loffay
 */
@DeploymentModules({
        @DeploymentModule(name = "io.opentracing.hawkular"),
        @DeploymentModule(name = "org.wildfly.swarm.opentracing.hawkular.jaxrs", slot = "main")
})
@Configurable("swarm.opentracing-hawkular")
public class OpenTracingHawkularFraction implements Fraction<OpenTracingHawkularFraction> {


    private OpenTracingHawkularResources subresources = new OpenTracingHawkularResources();

    public OpenTracingHawkularFraction() {
    }

    public OpenTracingHawkularResources subresources() {
        return this.subresources;
    }

    @Override
    public OpenTracingHawkularFraction applyDefaults() {
        tracing((tracing) -> {
            // nothing.
        });
        return this;
    }

    public OpenTracingHawkularFraction tracing(Consumer<Tracing> config) {
        this.subresources.tracing = new Tracing();
        config.accept(this.subresources.tracing);
        return this;
    }

    public Tracing tracing() {
        return this.subresources.tracing;
    }

    public static class OpenTracingHawkularResources {
        @SubresourceInfo("tracing")
        Tracing tracing;
    }

}

