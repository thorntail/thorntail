package org.jboss.unimbus.jpa.impl.opentracing;

import java.util.Collections;
import java.util.Map;

import javax.persistence.EntityManager;

import org.jboss.unimbus.TraceMode;
import org.jboss.unimbus.jpa.impl.EntityManagerResourceProvider;

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
        return new TracedEntityManager(this.traceMode, em);
    }

    private final TraceMode traceMode;
}
