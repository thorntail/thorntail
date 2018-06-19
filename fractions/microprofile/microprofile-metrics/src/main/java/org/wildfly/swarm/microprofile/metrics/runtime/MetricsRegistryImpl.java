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
package org.wildfly.swarm.microprofile.metrics.runtime;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

import javax.enterprise.inject.Vetoed;

import org.eclipse.microprofile.metrics.Counter;
import org.eclipse.microprofile.metrics.Gauge;
import org.eclipse.microprofile.metrics.Histogram;
import org.eclipse.microprofile.metrics.HitCounter;
import org.eclipse.microprofile.metrics.Metadata;
import org.eclipse.microprofile.metrics.Meter;
import org.eclipse.microprofile.metrics.Metric;
import org.eclipse.microprofile.metrics.MetricFilter;
import org.eclipse.microprofile.metrics.MetricRegistry;
import org.eclipse.microprofile.metrics.MetricType;
import org.eclipse.microprofile.metrics.ParallelCounter;
import org.eclipse.microprofile.metrics.Timer;
import org.jboss.logging.Logger;
import org.wildfly.swarm.microprofile.metrics.runtime.app.CounterImpl;
import org.wildfly.swarm.microprofile.metrics.runtime.app.ExponentiallyDecayingReservoir;
import org.wildfly.swarm.microprofile.metrics.runtime.app.HistogramImpl;
import org.wildfly.swarm.microprofile.metrics.runtime.app.MeterImpl;
import org.wildfly.swarm.microprofile.metrics.runtime.app.TimerImpl;

/**
 * @author hrupp
 */
@Vetoed
public class MetricsRegistryImpl extends MetricRegistry {

    private static final Logger LOGGER = Logger.getLogger(MetricsRegistryImpl.class);

    private Map<MKey, Metadata> metadataMap = new java.util.HashMap<>();
    private Map<MKey, Metric> metricMap = new ConcurrentHashMap<>();

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

        Metadata m = Metadata.builder().withName(name).withType(type).build();
        MKey key = new MKey(name,type);
        metricMap.put(key, metric);

        metadataMap.put(key, m);
        return metric;
    }

    @Override
    public <T extends Metric> T register(String name, T metric, Metadata metadata) throws IllegalArgumentException {

        Metadata newMeta = Metadata.builder(metadata).withName(name).build();

        return register(newMeta, metric);
    }

    @Override
    public <T extends Metric> T register(Metadata metadata, T metric) throws IllegalArgumentException {

        String name = metadata.getName();
        if (name == null) {
            throw new IllegalArgumentException("Metric name must not be null");
        }

        MKey key = new MKey(metadata.getName(),metadata.getTypeRaw());
        Metadata existingMetadata = metadataMap.get(key);
        boolean reusableFlag = (existingMetadata == null || existingMetadata.isReusable());

        //Gauges are not reusable
        if (metadata.getTypeRaw().equals(MetricType.GAUGE)) {
            reusableFlag = false;
        }

        if (metricMap.keySet().contains(key) && !reusableFlag) {
            throw new IllegalArgumentException("A metric with name " + metadata.getName() + " already exists");
        }

        metricMap.put(key, metric);
        metadataMap.put(key, metadata);

        return metric;
    }

    @Override
    public Counter counter(String name) {
        Metadata metadata = Metadata.builder().withName(name).withType(MetricType.COUNTER).build();
        return counter(metadata);
    }

    @Override
    public org.eclipse.microprofile.metrics.Counter counter(Metadata metadata) {
        return get(metadata, MetricType.COUNTER);
    }

    @Override
    public HitCounter hitCounter(String name) {
        Metadata metadata = Metadata.builder().withName(name).withType(MetricType.HIT_COUNTER).build();
        return hitCounter(metadata);
    }


    @Override
    public HitCounter hitCounter(Metadata metadata) {
        return get(metadata, MetricType.HIT_COUNTER);
    }


    @Override
    public ParallelCounter parallelCounter(String name) {
        Metadata metadata = Metadata.builder().withName(name).withType(MetricType.PARALLEL_COUNTER).build();
        return parallelCounter(metadata);
    }

    @Override
    public ParallelCounter parallelCounter(Metadata metadata) {
        return get(metadata, MetricType.PARALLEL_COUNTER);
    }



    @Override
    public Histogram histogram(String name) {
        Metadata metadata = Metadata.builder().withName(name).withType(MetricType.HISTOGRAM).build();
        return histogram(metadata);
    }

    @Override
    public Histogram histogram(Metadata metadata) {
        return get(metadata, MetricType.HISTOGRAM);
    }

    @Override
    public Meter meter(String name) {
        Metadata metadata = Metadata.builder().withName(name).withType(MetricType.METERED).build();
        return meter(metadata);
    }

    @Override
    public Meter meter(Metadata metadata) {
        return get(metadata, MetricType.METERED);
    }

    private <T extends Metric> T get(Metadata metadata, MetricType type) {
        String name = metadata.getName();
        MKey key = new MKey(metadata);
        LOGGER.debugf("Get metric [name: %s, type: %s]", name, type);
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Name must not be null or empty");
        }

        if (metadataMap.containsKey(key)) {
            Metadata existing = metadataMap.get(key);
            if (existing.isReusable() != metadata.isReusable()) {
                throw new IllegalArgumentException("Reusable setting has changed, must not happen");
            }
        }

        if (!metadataMap.containsKey(key)) {
            Metric m;
            switch (type) {

                case HIT_COUNTER:       // TODO different impl? -> not needed yet!
                case PARALLEL_COUNTER:
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
            LOGGER.infof("Register metric [name: %s, type: %s]", name, type);
            register(metadata, m);
        }

        return (T) metricMap.get(key);
    }

    @Override
    public Timer timer(String name) {
        Metadata metadata = Metadata.builder().withName(name).withType(MetricType.TIMER).build();
        return timer(metadata);
    }

    @Override
    public Timer timer(Metadata metadata) {
        return get(metadata, MetricType.TIMER);
    }

    public boolean remove(String name) {
        return false; // TODO implement
    }

    @Override
    public boolean unregister(String name, MetricType type) {
        return remove(new MKey(name, type));
    }

    @Override
    public boolean unregister(Metadata metadata) {
        return remove(new MKey(metadata));
    }

    private boolean remove(MKey metricKey) {
        if (metricMap.containsKey(metricKey)) {
            LOGGER.infof("Remove metric [key: %s]", metricKey);
            metricMap.remove(metricKey);
            metadataMap.remove(metricKey);
            return true;
        }
        return false;
    }

    @Override
    public void removeMatching(MetricFilter metricFilter) {
        Iterator<Map.Entry<MKey, Metric>> iterator = metricMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<MKey, Metric> entry = iterator.next();
            if (metricFilter.matches(entry.getKey().getName(), entry.getValue())) {  // TODO filter needs type (?)
                remove(entry.getKey());
            }
        }
    }

    @Override
    public java.util.SortedSet<String> getNames() {

        SortedSet<String> out = new TreeSet<>();
        for (MKey key : metricMap.keySet()) {
            out.add(key.getName());
        }

        return out;
    }

    @Override
    public java.util.SortedSet<String> getNames(MetricType type) {

        SortedSet<String> out = new TreeSet<>();
        for (MKey key : metricMap.keySet()) {
            if (key.getType().equals(type)) {
                out.add(key.getName());
            }
        }

        return out;
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
    public SortedMap<String, HitCounter> getHitCounters(MetricFilter metricFilter) {
        return getMetrics(MetricType.HIT_COUNTER, metricFilter);
    }

    @Override
    public SortedMap<String, HitCounter> getHitCounters() {
        return getHitCounters(MetricFilter.ALL);
    }

    @Override
    public SortedMap<String, ParallelCounter> getParallelCounters(MetricFilter metricFilter) {
        return getMetrics(MetricType.PARALLEL_COUNTER, metricFilter);
    }

    @Override
    public SortedMap<String, ParallelCounter> getParallelCounters() {
        return getParallelCounters(MetricFilter.ALL);
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
        Map<String,Metric> out = new HashMap<>();
        for (Map.Entry<MKey,Metric> entry: metricMap.entrySet()) {
            out.put(entry.getKey().getName(),entry.getValue());
        }

        return out;
    }

    private <T extends Metric> SortedMap<String, T> getMetrics(MetricType type, MetricFilter filter) {
        SortedMap<String, T> out = new TreeMap<>();

        for (Map.Entry<MKey, Metric> entry : metricMap.entrySet()) {
            Metadata metadata = metadataMap.get(entry.getKey());
            if (metadata.getTypeRaw().equals(type)) {
                if (filter.matches(entry.getKey().getName(), entry.getValue())) {
                    out.put(entry.getKey().getName(), (T) entry.getValue());
                }
            }
        }

        return out;
    }

    @Override
    public Map<String, Metadata> getMetadata() {
        Map<String,Metadata> out = new HashMap<>();
        for (Map.Entry<MKey,Metadata> entry: metadataMap.entrySet()) {
            out.put(entry.getKey().getName(),entry.getValue());
        }
        return out;
    }
}
