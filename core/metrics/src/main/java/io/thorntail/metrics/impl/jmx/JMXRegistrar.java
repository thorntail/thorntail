package io.thorntail.metrics.impl.jmx;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.eclipse.microprofile.metrics.Metric;
import org.eclipse.microprofile.metrics.MetricRegistry;
import org.eclipse.microprofile.metrics.MetricType;
import org.eclipse.microprofile.metrics.annotation.RegistryType;
import io.thorntail.events.LifecycleEvent;

import static io.thorntail.Info.ROOT_PACKAGE_PATH;

/**
 * Created by bob on 1/22/18.
 */
@ApplicationScoped
public class JMXRegistrar {

    private static final String JMX_METRIC_CONFIG_LOCATION = ROOT_PACKAGE_PATH + "/metrics/impl/jmx/";

    private static final String PROPERTIES_SUFFIX = ".properties";

    <T extends Metric> void init(@Observes LifecycleEvent.Initialize event) throws IOException, URISyntaxException {

        List<MBeanMetadata> configs = findMetadata();

        for (MBeanMetadata config : configs) {
            register(config);
        }
    }

    void register(MBeanMetadata config) {
        Metric metric = null;
        switch (config.getTypeRaw()) {
            case COUNTER:
                metric = new JMXCounter(this.helper, config.getMbean());
                break;
            case GAUGE:
                metric = new JMXGauge(this.helper, config.getMbean());
                break;
        }

        if (metric != null) {
            this.registry.register(config, metric);
        }
    }

    List<MBeanMetadata> findMetadata() throws IOException, URISyntaxException {
        URL locationUrl = getClass().getProtectionDomain().getCodeSource().getLocation();
        Path locationFile = Paths.get(locationUrl.toURI());

        List<MBeanMetadata> configs = null;
        if (Files.isDirectory(locationFile)) {
            configs = findConfigPropertiesFromDirectory(locationFile);
        } else {
            configs = findConfigPropertiesFromJar(locationFile);
        }

        this.helper.expandMultiValueEntries(configs);

        configs.sort(Comparator.comparing(e -> e.getName()));
        return configs;
    }

    List<MBeanMetadata> findConfigPropertiesFromDirectory(Path dir) throws IOException {
        List<MBeanMetadata> list = new ArrayList<>();

        FileVisitor<Path> visitor = new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Path relative = dir.relativize(file);
                String metricName = metricNameOf(relative.toString());
                if (metricName != null) {
                    try (InputStream in = new FileInputStream(file.toFile())) {
                        list.add(metadataOf(metricName, in));
                    }
                }
                return super.visitFile(file, attrs);
            }
        };

        Files.walkFileTree(dir, visitor);

        return list;
    }

    List<MBeanMetadata> findConfigPropertiesFromJar(Path file) throws IOException {
        List<MBeanMetadata> list = new ArrayList<>();
        try (JarFile jar = new JarFile(file.toFile())) {
            Enumeration<JarEntry> entries = jar.entries();

            while (entries.hasMoreElements()) {
                JarEntry each = entries.nextElement();

                String name = each.getName();

                String metricName = metricNameOf(name);
                if (metricName != null) {
                    try (InputStream in = jar.getInputStream(each)) {
                        list.add(metadataOf(metricName, in));
                    }
                }
            }
        }

        return list;
    }

    MBeanMetadata metadataOf(String name, InputStream in) throws IOException {
        Properties props = new Properties();
        props.load(in);
        return metadataOf(name, props);
    }

    MBeanMetadata metadataOf(String name, Properties props) {
        MBeanMetadata meta = new MBeanMetadata(name, metricTypeOf(props.getProperty("type")));
        meta.setMbean(props.getProperty("mbean"));
        meta.setDisplayName(props.getProperty("displayName"));
        meta.setDescription(props.getProperty("description"));
        meta.setUnit(props.getProperty("unit"));
        meta.setMulti("true".equalsIgnoreCase(props.getProperty("multi")));
        return meta;
    }

    MetricType metricTypeOf(String type) {
        return MetricType.valueOf(type.toUpperCase());
    }

    String metricNameOf(String path) {
        if (path.startsWith(JMX_METRIC_CONFIG_LOCATION) && path.endsWith(PROPERTIES_SUFFIX)) {
            String metricName = path.substring(JMX_METRIC_CONFIG_LOCATION.length());
            metricName = metricName.substring(0, metricName.length() - PROPERTIES_SUFFIX.length());
            return metricName;
        }
        return null;
    }

    @Inject
    @RegistryTarget(type = MetricRegistry.Type.BASE)
    Instance<MBeanMetadata> base;

    @Inject
    @RegistryType(type = MetricRegistry.Type.BASE)
    MetricRegistry registry;

    @Inject
    JMXHelper helper;
}
