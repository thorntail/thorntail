package org.wildfly.swarm.opentracing.hawkular.jaxrs;

import java.util.function.Consumer;

import org.wildfly.swarm.config.runtime.SubresourceInfo;
import org.wildfly.swarm.spi.api.Defaultable;

import static org.wildfly.swarm.spi.api.Defaultable.bool;
import static org.wildfly.swarm.spi.api.Defaultable.integer;

/**
 * Created by bob on 5/23/17.
 */
public class Tracing {


    private Defaultable<Integer> percentageSampling = integer(100);

    private String serviceName;

    private String buildStamp;

    private Defaultable<Boolean> consoleRecorder = bool(false);

    private TracingSubresources subresources = new TracingSubresources();

    public Tracing sampleRate(Integer percentage) {
        this.percentageSampling.set(percentage);
        return this;
    }

    public TracingSubresources subresources() {
        return this.subresources;
    }

    public Integer sampleRate() {
        return this.percentageSampling.get();
    }

    public Tracing serviceName(String serviceName) {
        this.serviceName = serviceName;
        return this;
    }

    public String serviceName() {
        return this.serviceName;
    }

    public Tracing buildStamp(String buildStamp) {
        this.buildStamp = buildStamp;
        return this;
    }

    public String buildStamp() {
        return this.buildStamp;
    }

    public Tracing consoleRecorder(Boolean consoleRecorder) {
        this.consoleRecorder.set(consoleRecorder);
        return this;
    }

    public Boolean consoleRecorder() {
        return this.consoleRecorder.get();
    }

    public Tracing traceRecorder(Consumer<TraceRecorder> config) {
        TraceRecorder traceRecorder = new TraceRecorder();
        config.accept(traceRecorder);
        this.subresources.traceRecorder = traceRecorder;
        return this;
    }

    public TraceRecorder traceRecorder() {
        return this.subresources.traceRecorder;
    }

    public static class TracingSubresources {
        @SubresourceInfo("trace-recorder")
        private TraceRecorder traceRecorder;
    }

}
