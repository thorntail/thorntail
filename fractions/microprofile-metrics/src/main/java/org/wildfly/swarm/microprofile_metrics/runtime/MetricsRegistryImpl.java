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
import org.eclipse.microprofile.metrics.Metadata;
import org.eclipse.microprofile.metrics.Metric;
import org.eclipse.microprofile.metrics.Timer;
import org.wildfly.swarm.microprofile_metrics.runtime.app.CounterImpl;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author hrupp
 */
public class MetricsRegistryImpl extends org.eclipse.microprofile.metrics.MetricRegistry {

  private Map<String, Metadata> metadataMap = new java.util.HashMap<>();
  private Map<String, Metric> metricMap = new ConcurrentHashMap<>();

  @Override
  public <T extends Metric> T register(String name, T metric) throws IllegalArgumentException {

    if (metricMap.keySet().contains(name)) {
      throw new IllegalArgumentException("A metric with name " + name + " already exists");
    }

    Metadata m = new Metadata(name, org.eclipse.microprofile.metrics.MetricType.from(metric.getClass()));
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
  public org.eclipse.microprofile.metrics.Counter counter(String counterName) {
    if (!metadataMap.containsKey(counterName)) {
      register(counterName,new CounterImpl());
    }
    return (org.eclipse.microprofile.metrics.Counter) metricMap.get(counterName);
  }

  @Override
  public org.eclipse.microprofile.metrics.Counter counter(Metadata metadata) {
    return null;  // TODO: Customise this generated block
  }

  @Override
  public org.eclipse.microprofile.metrics.Histogram histogram(String s) {
    return null;  // TODO: Customise this generated block
  }

  @Override
  public org.eclipse.microprofile.metrics.Histogram histogram(Metadata metadata) {
    return null;  // TODO: Customise this generated block
  }

  @Override
  public org.eclipse.microprofile.metrics.Meter meter(String s) {
    return null;  // TODO: Customise this generated block
  }

  @Override
  public org.eclipse.microprofile.metrics.Meter meter(Metadata metadata) {
    return null;  // TODO: Customise this generated block
  }

  @Override
  public Timer timer(String s) {
    return null;  // TODO: Customise this generated block
  }

  @Override
  public Timer timer(Metadata metadata) {
    return null;  // TODO: Customise this generated block
  }

  @Override
  public boolean remove(String s) {
    return false;  // TODO: Customise this generated block
  }

  @Override
  public void removeMatching(org.eclipse.microprofile.metrics.MetricFilter metricFilter) {
    // TODO: Customise this generated block
  }

  @Override
  public java.util.SortedSet<String> getNames() {
    return new java.util.TreeSet<>(metricMap.keySet());
  }

  @Override
  public SortedMap<String, Gauge> getGauges() {
    return null;  // TODO: Customise this generated block
  }

  @Override
  public SortedMap<String, Gauge> getGauges(org.eclipse.microprofile.metrics.MetricFilter metricFilter) {
    return null;  // TODO: Customise this generated block
  }

  @Override
  public SortedMap<String, Counter> getCounters() {
    return null;  // TODO: Customise this generated block
  }

  @Override
  public SortedMap<String, Counter> getCounters(org.eclipse.microprofile.metrics.MetricFilter metricFilter) {
    return null;  // TODO: Customise this generated block
  }

  @Override
  public java.util.SortedMap<String, org.eclipse.microprofile.metrics.Histogram> getHistograms() {
    return null;  // TODO: Customise this generated block
  }

  @Override
  public java.util.SortedMap<String, org.eclipse.microprofile.metrics.Histogram> getHistograms(org.eclipse.microprofile.metrics.MetricFilter metricFilter) {
    return null;  // TODO: Customise this generated block
  }

  @Override
  public java.util.SortedMap<String, org.eclipse.microprofile.metrics.Meter> getMeters() {
    return null;  // TODO: Customise this generated block
  }

  @Override
  public java.util.SortedMap<String, org.eclipse.microprofile.metrics.Meter> getMeters(org.eclipse.microprofile.metrics.MetricFilter metricFilter) {
    return null;  // TODO: Customise this generated block
  }

  @Override
  public java.util.SortedMap<String, org.eclipse.microprofile.metrics.Timer> getTimers() {
    return null;  // TODO: Customise this generated block
  }

  @Override
  public java.util.SortedMap<String, org.eclipse.microprofile.metrics.Timer> getTimers(org.eclipse.microprofile.metrics.MetricFilter metricFilter) {
    return null;  // TODO: Customise this generated block
  }

  @Override
  public Map<String, Metric> getMetrics() {
    Map<String, Metric> out = new HashMap<>(metricMap);

    return out;
  }

  @Override
  public Map<String, Metadata> getMetadata() {
    return metadataMap; // TODO return immutable object
  }
}
