package org.wildfly.swarm.opentracing.hawkular.jaxrs;

import java.util.function.Consumer;

import org.wildfly.swarm.config.runtime.SubresourceInfo;
import org.wildfly.swarm.spi.api.Defaultable;

import static org.wildfly.swarm.spi.api.Defaultable.integer;

/**
 * Created by bob on 5/23/17.
 */
public class TraceRecorder {


    private TraceRecorderResources subresources = new TraceRecorderResources();

    public TraceRecorder() {

    }

    public TraceRecorder batchSize(Integer batchSize) {
        this.batchSize.set(batchSize);
        return this;
    }

    public Integer batchSize() {
        return this.batchSize.get();
    }

    public TraceRecorder batchTime(Integer batchTime) {
        this.batchTime.set(batchTime);
        return this;
    }

    public Integer batchTime() {
        return this.batchTime.get();
    }

    public TraceRecorder threadPoolSize(Integer threadPoolSize) {
        this.threadPoolSize.set(threadPoolSize);
        return this;
    }

    public Integer threadPoolSize() {
        return this.threadPoolSize.get();
    }

    public TraceRecorder tenantId(String tenantId) {
        this.tenantId = tenantId;
        return this;
    }

    public String tenantId() {
        return this.tenantId;
    }

    public TraceRecorderResources subresources() {
        return this.subresources;
    }

    public TraceRecorder httpTracePublisher(Consumer<HTTPTracePublisher> config) {
        HTTPTracePublisher publisher = new HTTPTracePublisher();
        config.accept(publisher);
        this.subresources.httpTracePublisher = publisher;
        return this;
    }

    public HTTPTracePublisher httpTracePublisher() {
        return this.subresources.httpTracePublisher;
    }

    private Defaultable<Integer> batchSize = integer(1000);

    private Defaultable<Integer> batchTime = integer(500);

    private Defaultable<Integer> threadPoolSize = integer(5);

    private String tenantId;

    public static class TraceRecorderResources {
        @SubresourceInfo("http-publisher")
        private HTTPTracePublisher httpTracePublisher;
    }
}
