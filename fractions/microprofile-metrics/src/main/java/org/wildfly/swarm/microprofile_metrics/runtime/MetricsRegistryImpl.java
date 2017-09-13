/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wildfly.swarm.microprofile_metrics.runtime;

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
import org.wildfly.swarm.microprofile_metrics.runtime.app.CounterImpl;
import org.wildfly.swarm.microprofile_metrics.runtime.app.ExponentiallyDecayingReservoir;
import org.wildfly.swarm.microprofile_metrics.runtime.app.GaugeImpl;
import org.wildfly.swarm.microprofile_metrics.runtime.app.HistogramImpl;
import org.wildfly.swarm.microprofile_metrics.runtime.app.MeterImpl;
import org.wildfly.swarm.microprofile_metrics.runtime.app.TimerImpl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author hrupp
 */
public class MetricsRegistryImpl extends MetricRegistry {

  private Map<String, Metadata> metadataMap = new java.util.HashMap<>();
  private Map<String, Metric> metricMap = new ConcurrentHashMap<>();

  @Override
  public <T extends Metric> T register(String name, T metric) throws IllegalArgumentException {

    if (metricMap.keySet().contains(name)) {
      throw new IllegalArgumentException("A metric with name " + name + " already exists");
    }

    MetricType type;
    if (metric.getClass().getName().contains("Lambda")) {
      String tname = metric.getClass().getGenericInterfaces()[0].getTypeName(); // TODO [0] is brittle
      tname = tname.substring(tname.lastIndexOf('.') + 1);
      tname = tname.toLowerCase();
      type = MetricType.from(tname);
    } else {
      if (!metric.getClass().isInterface()) {
        // [0] is ok, as all our Impl classes implement exactly the one matching interface
        type = MetricType.from(metric.getClass().getInterfaces()[0]);
      } else {
        type = MetricType.from(metric.getClass());
      }
    }

    Metadata m = new Metadata(name, type);
    metricMap.put(name,metric);

    metadataMap.put(name,m);
    return metric;
  }

  @Override
  public <T extends Metric> T register(String name, T metric, Metadata metadata) throws IllegalArgumentException {

    if (metricMap.keySet().contains(name)) {
      throw new IllegalArgumentException("A metric with name " + name + " already exists");
    }

    metricMap.put(name,metric);
    metadataMap.put(name,metadata);

    return metric;
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
     if (!metadataMap.containsKey(name)) {
       System.err.println("+++ " + name + " was not yet registered");
       Metric m;
       switch (type) {

         case COUNTER: m = new CounterImpl();
           break;
         case GAUGE: m = new GaugeImpl();
           break;
         case METERED: m = new MeterImpl();
           break;
         case HISTOGRAM: m = new HistogramImpl(new ExponentiallyDecayingReservoir());
           break;
         case TIMER: m = new TimerImpl(new ExponentiallyDecayingReservoir());
           break;
         case INVALID:
         default:
           throw new  IllegalStateException("Must not happen");
       }
       register(name,m, metadata);
     }
     return (T) metricMap.get(name);
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
  public boolean remove(String s) {
    if (metricMap.containsKey(s)) {
      metricMap.remove(s);
      metadataMap.remove(s); // TODO really?
      return true;
    }
    return false;
  }

  @Override
  public void removeMatching(MetricFilter metricFilter) {
    // TODO: Customise this generated block
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
    return getMetrics(MetricType.COUNTER,metricFilter);
  }

  @Override
  public java.util.SortedMap<String, Histogram> getHistograms() {
    return getHistograms(MetricFilter.ALL);
  }

  @Override
  public java.util.SortedMap<String, Histogram> getHistograms(MetricFilter metricFilter) {
    return getMetrics(MetricType.HISTOGRAM,metricFilter);
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
    return getMetrics(MetricType.TIMER,metricFilter);
  }

  @Override
  public Map<String, Metric> getMetrics() {
    Map<String, Metric> out = new HashMap<>(metricMap);

    return out;
  }

  private <T extends Metric> SortedMap<String, T> getMetrics(MetricType type, MetricFilter filter) {
    Set<String> names = new HashSet<>();
    for (Metadata metadata : getMetadata().values()) {
      if (!metadata.getTypeRaw().equals(type)) {
        continue;
      }
      // TODO add filter
      names.add(metadata.getName());
    }

    SortedMap<String, T> metrics = new TreeMap<String, T>();
    for (String name : names) {
      metrics.put(name, (T) this.metricMap.get(name));
    }

    return metrics;
  }

  @Override
  public Map<String, Metadata> getMetadata() {
    return metadataMap; // TODO return immutable object
  }
}
