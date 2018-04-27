package io.thorntail.metrics.impl.jmx;

import javax.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.metrics.MetricRegistry;
import org.eclipse.microprofile.metrics.MetricType;

/**
 * Created by bob on 1/22/18.
 */
@ApplicationScoped
@RegistryTarget(type = MetricRegistry.Type.BASE)
public class CurrentLoadedClassCount extends MBeanMetadata {

    CurrentLoadedClassCount() {
        super("classloader.currentLoadedClass.count",
              MetricType.COUNTER);
        setDisplayName("Current Loaded Class Count");
        setDescription("Displays the number of classes that are currently loaded in the Java virtual machine.");
        setUnit("none");
        setMbean("java.lang:type=ClassLoading/LoadedClassCount");
    }
}
