package io.thorntail.jpa.impl.opentracing;

import java.util.Collections;
import java.util.Map;

import javax.persistence.EntityManager;

import io.thorntail.jpa.impl.EntityManagerResourceProvider;
import io.thorntail.TraceMode;

/**
 * Created by bob on 2/27/18.
 */
public class TracedEntityManagerResourceProvider extends EntityManagerResourceProvider {
    public TracedEntityManagerResourceProvider(TraceMode traceMode, String unitName) {
        this(traceMode, unitName, Collections.emptyMap());
    }

    public TracedEntityManagerResourceProvider(TraceMode traceMode, String unitName, Map<String,String> properties) {
        super(unitName, properties);
        this.traceMode = traceMode;
    }

    @Override
    protected EntityManager wrap(EntityManager em) {
        return super.wrap(new TracedEntityManager(this.traceMode, em));
    }

    private final TraceMode traceMode;
}
