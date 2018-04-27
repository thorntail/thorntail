package io.thorntail.metrics.impl;

import org.eclipse.microprofile.metrics.Metadata;
import org.eclipse.microprofile.metrics.MetricType;

/**
 * Created by bob on 2/5/18.
 */
public class OriginTrackedMetadata extends Metadata {
    public OriginTrackedMetadata(Object origin, String name, MetricType type) {
        super(name, type);
        this.origin = origin;
    }

    public Object getOrigin() {
        return this.origin;
    }

    private final Object origin;
}
