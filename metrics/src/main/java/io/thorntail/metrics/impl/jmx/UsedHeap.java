package io.thorntail.metrics.impl.jmx;

import javax.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.metrics.MetricRegistry;
import org.eclipse.microprofile.metrics.MetricType;

/**
 * Created by bob on 1/22/18.
 */
@ApplicationScoped
@RegistryTarget(type = MetricRegistry.Type.BASE)
public class UsedHeap extends MBeanMetadata {

    UsedHeap() {
        super("memory.usedHeap",
              MetricType.GAUGE);
        setUnit("bytes");
        setMbean("java.lang:type=Memory/HeapMemoryUsage#used");
    }
}
