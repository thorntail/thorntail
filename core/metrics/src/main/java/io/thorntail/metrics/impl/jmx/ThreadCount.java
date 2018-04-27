package io.thorntail.metrics.impl.jmx;

import javax.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.metrics.MetricRegistry;
import org.eclipse.microprofile.metrics.MetricType;

/**
 * Created by bob on 1/22/18.
 */
@ApplicationScoped
@RegistryTarget(type = MetricRegistry.Type.BASE)
public class ThreadCount extends MBeanMetadata {

    ThreadCount() {
        super("thread.count", MetricType.COUNTER);
        setDescription("Number of currently deployed threads");
        setDisplayName("Current Thread count");
        setUnit("none");
        setMbean("java.lang:type=Threading/ThreadCount");
    }
}
