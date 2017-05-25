package org.wildfly.swarm.opentracing.hawkular.jaxrs.runtime;

import java.util.ArrayList;
import java.util.List;

import org.hawkular.apm.api.model.trace.Trace;
import org.hawkular.apm.client.api.recorder.TraceRecorder;

/**
 * Created by bob on 5/24/17.
 */
public class MultiTraceRecorder implements TraceRecorder {

    public MultiTraceRecorder() {

    }

    public void add(TraceRecorder recorder) {
        this.recorders.add(recorder);
    }

    public int size() {
        return this.recorders.size();
    }

    public boolean isEmpty() {
        return this.recorders.isEmpty();
    }

    @Override
    public void record(Trace trace) {
        this.recorders.forEach(r -> {
            r.record(trace);
        });
    }

    private List<TraceRecorder> recorders = new ArrayList<>();
}
