/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */
package org.wildfly.swarm.microprofile.metrics.deployment;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.eclipse.microprofile.metrics.Counter;
import org.eclipse.microprofile.metrics.Gauge;
import org.eclipse.microprofile.metrics.Histogram;
import org.eclipse.microprofile.metrics.Metadata;
import org.eclipse.microprofile.metrics.Meter;
import org.eclipse.microprofile.metrics.MetricRegistry;
import org.eclipse.microprofile.metrics.MetricType;
import org.eclipse.microprofile.metrics.Timer;
import org.eclipse.microprofile.metrics.annotation.Metric;
import org.eclipse.microprofile.metrics.annotation.RegistryType;
import org.wildfly.swarm.microprofile.metrics.api.RegistryFactory;

/**
 * @author hrupp
 */
@ApplicationScoped
public class MetricProducer {

    @Inject
    private MetricName metricName;

    private ConcurrentMap<MetricRegistry.Type, MetricRegistry> registries;

    @PostConstruct
    void init() {
        registries = new ConcurrentHashMap<>();
    }

    @Default
    @RegistryType(type = MetricRegistry.Type.APPLICATION)
    @Produces
    MetricRegistry getApplicationRegistry() {
        return get(MetricRegistry.Type.APPLICATION);
    }

    @RegistryType(type = MetricRegistry.Type.BASE)
    @Produces
    MetricRegistry getBaseRegistry() {
        return get(MetricRegistry.Type.BASE);
    }

    @RegistryType(type = MetricRegistry.Type.VENDOR)
    @Produces
    MetricRegistry getVendorRegistry() {
        return get(MetricRegistry.Type.VENDOR);
    }

    @Produces
    <T> Gauge<T> getGauge(InjectionPoint ip) {
        // A forwarding Gauge must be returned as the Gauge creation happens when the declaring bean gets instantiated and the corresponding Gauge can be injected before which leads to producing a null value
        return new Gauge<T>() {
            @Override
            @SuppressWarnings("unchecked")
            public T getValue() {
                // TODO: better error report when the gauge doesn't exist
                return ((Gauge<T>) getApplicationRegistry().getGauges().get(metricName.of(ip))).getValue();
            }
        };
    }

    @Produces
    Counter getCounter(InjectionPoint ip) {
        return getApplicationRegistry().counter(getMetadata(ip, MetricType.COUNTER));
    }

    @Produces
    Histogram getHistogram(InjectionPoint ip) {
        return getApplicationRegistry().histogram(getMetadata(ip, MetricType.HISTOGRAM));
    }

    @Produces
    Meter getMeter(InjectionPoint ip) {
        return getApplicationRegistry().meter(getMetadata(ip, MetricType.METERED));
    }

    @Produces
    Timer getTimer(InjectionPoint ip) {
        return getApplicationRegistry().timer(getMetadata(ip, MetricType.TIMER));
    }

    public MetricRegistry get(MetricRegistry.Type type) {
        return registries.computeIfAbsent(type, key -> {
            try {
                InitialContext context = new InitialContext();
                Object o = context.lookup("jboss/swarm/metrics");
                RegistryFactory factory = (RegistryFactory) o;
                return factory.get(type);
            } catch (NamingException e) {
                throw new IllegalStateException("RegistryFactory not found");
            }
        });
    }

    private Metadata getMetadata(InjectionPoint ip, MetricType type) {
        Metadata metadata = new Metadata(metricName.of(ip), type);
        Metric metric = ip.getAnnotated().getAnnotation(Metric.class);
        if (metric != null) {
            if (!metric.unit().isEmpty()) {
                metadata.setUnit(metric.unit());
            }
            if (!metric.description().isEmpty()) {
                metadata.setDescription(metric.description());
            }
            if (!metric.displayName().isEmpty()) {
                metadata.setDisplayName(metric.displayName());
            }
            if (metric.tags().length > 0) {
                for (String tag : metric.tags()) {
                    metadata.addTags(tag);
                }
            }
        }
        return metadata;
    }
}
