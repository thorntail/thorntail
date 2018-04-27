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
 */
package io.thorntail.metrics.impl;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

import javax.enterprise.inject.spi.InjectionPoint;

import io.thorntail.metrics.impl.app.CounterImpl;
import io.thorntail.metrics.impl.app.ExponentiallyDecayingReservoir;
import org.eclipse.microprofile.metrics.Counter;
import org.eclipse.microprofile.metrics.Gauge;
import org.eclipse.microprofile.metrics.Histogram;
import org.eclipse.microprofile.metrics.Metadata;
import org.eclipse.microprofile.metrics.Meter;
import org.eclipse.microprofile.metrics.Metric;
import org.eclipse.microprofile.metrics.MetricFilter;
import org.eclipse.microprofile.metrics.MetricRegistry;
import org.eclipse.microprofile.metrics.MetricType;
import org.eclipse.microprofile.metrics.Timer;
import io.thorntail.metrics.impl.app.HistogramImpl;
import io.thorntail.metrics.impl.app.MeterImpl;
import io.thorntail.metrics.impl.app.TimerImpl;

/**
 * @author hrupp
 */
public class MetricsRegistryImpl extends MetricRegistry {


    private final Type type;

    private Map<String, Metadata> metadataMap = new HashMap<>();

    private Map<String, Metric> metricMap = new ConcurrentHashMap<>();

    public MetricsRegistryImpl(MetricRegistry.Type type) {
        this.type = type;
    }

    @Override
    public <T extends Metric> T register(String name, T metric) throws IllegalArgumentException {

        if (metricMap.keySet().contains(name)) {
            throw new IllegalArgumentException("A metric with name " + name + " already exists");
        }

        MetricType type;
        Class<?> metricCls = metric.getClass();
        if (metricCls.getName().contains("Lambda")) {
            String tname = metricCls.getGenericInterfaces()[0].getTypeName(); // TODO [0] is brittle
            tname = tname.substring(tname.lastIndexOf('.') + 1);
            tname = tname.toLowerCase();
            type = MetricType.from(tname);
        } else if (metricCls.isAnonymousClass()) {
            type = MetricType.from(metricCls.getInterfaces().length == 0 ? metricCls.getSuperclass().getInterfaces()[0] : metricCls.getInterfaces()[0]);
        } else {
            if (!metricCls.isInterface()) {
                // [0] is ok, as all our Impl classes implement exactly the one matching interface
                type = MetricType.from(metricCls.getInterfaces()[0]);
            } else {
                type = MetricType.from(metricCls);
            }
        }

        Metadata m = new Metadata(name, type);
        metricMap.put(name, metric);

        metadataMap.put(name, m);
        return metric;
    }

    @Override
    public <T extends Metric> T register(String name, T metric, Metadata metadata) throws IllegalArgumentException {
        metadata.setName(name);
        return register(metadata, metric);
    }

    @Override
    public <T extends Metric> T register(Metadata metadata, T metric) throws IllegalArgumentException {

        String name = metadata.getName();
        if (name == null) {
            throw new IllegalArgumentException("Metric name must not be null");
        }

        Metadata existingMetadata = metadataMap.get(name);
        boolean reusableFlag = (existingMetadata == null || existingMetadata.isReusable());

        //Gauges are not reusable
        if (metadata.getTypeRaw().equals(MetricType.GAUGE)) {
            reusableFlag = false;
        }

        if (metricMap.keySet().contains(metadata.getName()) && !reusableFlag) {
            throw new IllegalArgumentException(" A metric with name " + metadata.getName() + " already exists");
        }

        if (existingMetadata != null && !existingMetadata.getTypeRaw().equals(metadata.getTypeRaw())) {
            throw new IllegalArgumentException("Passed metric type does not match existing type");
        }

        if (existingMetadata != null && (existingMetadata.isReusable() != metadata.isReusable())) {
            throw new IllegalArgumentException("Reusable flag differs from previous usage");
        }

        metricMap.put(name, metric);
        metadataMap.put(name, duplicate(metadata));

        MetricsMessages.MESSAGES.registeredMetric(this.type.getName(), name);

        return metric;
    }

    protected Metadata duplicate(Metadata meta) {
        Metadata copy = null;
        if (meta instanceof OriginTrackedMetadata) {
            copy = new OriginTrackedMetadata(((OriginTrackedMetadata) meta).getOrigin(), meta.getName(), meta.getTypeRaw());
        } else {
            copy = new Metadata(meta.getName(), meta.getTypeRaw());
        }
        copy.setDescription(meta.getDescription());
        copy.setUnit(meta.getUnit());
        copy.setDisplayName(meta.getDisplayName());
        copy.setReusable(meta.isReusable());

        HashMap<String, String> tagsCopy = new HashMap<>();
        tagsCopy.putAll(meta.getTags());
        copy.setTags(tagsCopy);
        return copy;
    }

    @Override
    public Counter counter(String name) {
        return counter(new Metadata(name, MetricType.COUNTER));
    }

    @Override
    public org.eclipse.microprofile.metrics.Counter counter(Metadata metadata) {
        return get(metadata, MetricType.COUNTER);
    }

    @Override
    public Histogram histogram(String name) {
        return histogram(new Metadata(name, MetricType.HISTOGRAM));
    }

    @Override
    public Histogram histogram(Metadata metadata) {
        return get(metadata, MetricType.HISTOGRAM);
    }

    @Override
    public Meter meter(String s) {
        return meter(new Metadata(s, MetricType.METERED));
    }

    @Override
    public Meter meter(Metadata metadata) {
        return get(metadata, MetricType.METERED);
    }

    private <T extends Metric> T get(Metadata metadata, MetricType type) {
        String name = metadata.getName();
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Name must not be null or empty");
        }

        Metadata previous = metadataMap.get(name);

        if (previous == null) {
            Metric m;
            switch (type) {

                case COUNTER:
                    m = new CounterImpl();
                    break;
                case GAUGE:
                    throw new IllegalArgumentException("Gauge " + name + " was not registered, this should not happen");
                case METERED:
                    m = new MeterImpl();
                    break;
                case HISTOGRAM:
                    m = new HistogramImpl(new ExponentiallyDecayingReservoir());
                    break;
                case TIMER:
                    m = new TimerImpl(new ExponentiallyDecayingReservoir());
                    break;
                case INVALID:
                default:
                    throw new IllegalStateException("Must not happen");
            }
            register(metadata, m);
        } else if (!previous.getTypeRaw().equals(metadata.getTypeRaw())) {
            throw new IllegalArgumentException("Type of existing previously registered metric " + name + " does not " +
                                                       "match passed type");
        } else if ( haveCompatibleOrigins(previous, metadata)) {
            // stop caring, same thing.
        } else if (previous.isReusable() && !metadata.isReusable()) {
            throw new IllegalArgumentException("Previously registered metric " + name + " was flagged as reusable, while current request is not.");
        } else if (!previous.isReusable()) {
            throw new IllegalArgumentException("Previously registered metric " + name + " was not flagged as reusable");
        }

        return (T) metricMap.get(name);
    }

    private boolean haveCompatibleOrigins(Metadata left, Metadata right) {
        if ( left instanceof OriginTrackedMetadata && right instanceof OriginTrackedMetadata ) {
            OriginTrackedMetadata leftOrigin = (OriginTrackedMetadata) left;
            OriginTrackedMetadata rightOrigin = (OriginTrackedMetadata) right;

            if ( leftOrigin.getOrigin().equals(rightOrigin.getOrigin())) {
                return true;
            }

            if ( leftOrigin.getOrigin() instanceof InjectionPoint || ((OriginTrackedMetadata) right).getOrigin() instanceof InjectionPoint ) {
                return true;
            }

        }

        return false;

    }

    @Override
    public Timer timer(String s) {
        return timer(new Metadata(s, MetricType.TIMER));
    }

    @Override
    public Timer timer(Metadata metadata) {
        return get(metadata, MetricType.TIMER);
    }

    @Override
    public boolean remove(String metricName) {
        if (metricMap.containsKey(metricName)) {
            metricMap.remove(metricName);
            metadataMap.remove(metricName);
            return true;
        }
        return false;
    }

    @Override
    public void removeMatching(MetricFilter metricFilter) {
        Iterator<Map.Entry<String, Metric>> iterator = metricMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Metric> entry = iterator.next();
            if (metricFilter.matches(entry.getKey(), entry.getValue())) {
                remove(entry.getKey());
            }
        }
    }

    @Override
    public java.util.SortedSet<String> getNames() {
        return new java.util.TreeSet<>(metricMap.keySet());
    }

    @Override
    public SortedMap<String, Gauge> getGauges() {
        return getGauges(MetricFilter.ALL);
    }

    @Override
    public SortedMap<String, Gauge> getGauges(MetricFilter metricFilter) {
        return getMetrics(MetricType.GAUGE, metricFilter);
    }

    @Override
    public SortedMap<String, Counter> getCounters() {
        return getCounters(MetricFilter.ALL);
    }

    @Override
    public SortedMap<String, Counter> getCounters(MetricFilter metricFilter) {
        return getMetrics(MetricType.COUNTER, metricFilter);
    }

    @Override
    public java.util.SortedMap<String, Histogram> getHistograms() {
        return getHistograms(MetricFilter.ALL);
    }

    @Override
    public java.util.SortedMap<String, Histogram> getHistograms(MetricFilter metricFilter) {
        return getMetrics(MetricType.HISTOGRAM, metricFilter);
    }

    @Override
    public java.util.SortedMap<String, Meter> getMeters() {
        return getMeters(MetricFilter.ALL);
    }

    @Override
    public java.util.SortedMap<String, Meter> getMeters(MetricFilter metricFilter) {
        return getMetrics(MetricType.METERED, metricFilter);
    }

    @Override
    public java.util.SortedMap<String, Timer> getTimers() {
        return getTimers(MetricFilter.ALL);
    }

    @Override
    public java.util.SortedMap<String, Timer> getTimers(MetricFilter metricFilter) {
        return getMetrics(MetricType.TIMER, metricFilter);
    }

    @Override
    public Map<String, Metric> getMetrics() {

        return new HashMap<>(metricMap);
    }

    private <T extends Metric> SortedMap<String, T> getMetrics(MetricType type, MetricFilter filter) {
        SortedMap<String, T> out = new TreeMap<String, T>();

        Iterator<Map.Entry<String, Metric>> iterator = metricMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Metric> entry = iterator.next();
            if (filter.matches(entry.getKey(), entry.getValue())) {
                out.put(entry.getKey(), (T) entry.getValue());
            }
        }

        return out;
    }

    @Override
    public Map<String, Metadata> getMetadata() {
        return new HashMap<>(metadataMap);
    }
}
