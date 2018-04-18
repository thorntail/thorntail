package io.thorntail.metrics.impl.jmx;

import javax.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.metrics.MetricRegistry;
import org.eclipse.microprofile.metrics.MetricType;

/**
 * Created by bob on 1/22/18.
 */
@ApplicationScoped
@RegistryTarget(type = MetricRegistry.Type.BASE)
public class DaemonThreadCount extends MBeanMetadata {

    DaemonThreadCount() {
        super("thread.daemon.count", MetricType.COUNTER);
        setDescription("Displays the current number of live daemon threads.");
        setDisplayName("Daemon Thread Count");
        setUnit("none");
        setMbean("java.lang:type=Threading/DaemonThreadCount");
    }
}
