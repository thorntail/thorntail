package io.thorntail.metrics.impl.jmx;

import javax.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.metrics.MetricRegistry;
import org.eclipse.microprofile.metrics.MetricType;

/**
 * Created by bob on 1/22/18.
 */
@ApplicationScoped
@RegistryTarget(type = MetricRegistry.Type.BASE)
public class AvailableProcessors extends MBeanMetadata {

    AvailableProcessors() {
        super("cpu.availableProcessors",
              MetricType.GAUGE);
        setDisplayName("Available Processors");
        setDescription("Displays the number of processors available to the Java virtual machine. This value may change during a particular invocation of the virtual machine.");
        setUnit("none");
        setMbean("java.lang:type=OperatingSystem/AvailableProcessors");
    }
}
