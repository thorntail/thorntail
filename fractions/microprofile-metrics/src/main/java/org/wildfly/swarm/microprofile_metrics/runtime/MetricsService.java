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

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.eclipse.microprofile.metrics.Metadata;
import org.eclipse.microprofile.metrics.MetricRegistry;
import org.eclipse.microprofile.metrics.MetricType;
import org.eclipse.microprofile.metrics.MetricUnits;
import org.jboss.as.controller.ModelController;

import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.as.server.ServerEnvironment;
import org.jboss.logging.Logger;
import org.jboss.msc.inject.Injector;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;

/**
 * @author Heiko W. Rupp
 */
public class MetricsService implements Service<MetricsService> {

  private static Logger LOG = Logger.getLogger("org.wildfly.swarm.microprofile_metrics");
  private ExecutorService executorService;
  private ModelControllerClient controllerClient;

  public static final ServiceName SERVICE_NAME = ServiceName.of("swarm", "mp-metrics");

  private final InjectedValue<ServerEnvironment> serverEnvironmentValue = new InjectedValue<ServerEnvironment>();
  private final InjectedValue<ModelController> modelControllerValue = new InjectedValue<ModelController>();

  MetricRegistry baseRegistry = MetricRegistryFactory.get(MetricRegistry.Type.BASE);

  @Override
  public void start(StartContext context) throws StartException {
    executorService = Executors.newSingleThreadExecutor();
    controllerClient = modelControllerValue.getValue().createClient(executorService);

    BaseMetricWorker baseMetricWorker = BaseMetricWorker.create(controllerClient);

    registerBaseMetrics();
    registerGarbageCollectors(baseMetricWorker);
    LOG.info("MicroProfile-Metrics started");
  }

  @Override
  public void stop(StopContext context) {
    if (executorService != null) {
      executorService.shutdown();
    }
  }

  @Override
  public MetricsService getValue() throws IllegalStateException, IllegalArgumentException {
    return this;
  }

  /**
   * Register the metrics of the base scope with the system.
   */
  private void registerBaseMetrics() {
    baseRegistry.getMetadata().put("thread.count", new Metadata("thread.count", MetricType.GAUGE));
    baseRegistry.getMetadata().put("thread.daemon.count", new Metadata("thread.count", MetricType.GAUGE));
    baseRegistry.getMetadata().put("thread.max.count", new Metadata("thread.max.count", MetricType.GAUGE));

    baseRegistry.getMetadata().put("memory.maxHeap", new Metadata("memory.maxHeap", MetricType.GAUGE, "bytes"));
    baseRegistry.getMetadata().put("memory.usedHeap", new Metadata("memory.usedHeap", MetricType.GAUGE, "bytes"));
    baseRegistry.getMetadata().put("memory.committedHeap", new Metadata("memory.committedHeap", MetricType.GAUGE, "bytes"));

    baseRegistry.getMetadata().put("classloader.currentLoadedClass.count",
                                   new Metadata("classloader.currentLoadedClass.count", MetricType.COUNTER, MetricUnits.NONE));
    baseRegistry.getMetadata().put("classloader.totalLoadedClass.count",
                                   new Metadata("classloader.totalLoadedClass.count", MetricType.COUNTER, MetricUnits.NONE));
    baseRegistry.getMetadata().put("classloader.totalUnloadedClass.count",
                                   new Metadata("classloader.totalUnloadedClass.count", MetricType.COUNTER, MetricUnits.NONE));

    baseRegistry.getMetadata().put("cpu.availableProcessors",
                                   new Metadata("cpu.availableProcessors", MetricType.GAUGE, MetricUnits.NONE));
    baseRegistry.getMetadata().put("cpu.systemLoadAverage",
                                   new Metadata("cpu.systemLoadAverage", MetricType.GAUGE, MetricUnits.NONE));
    baseRegistry.getMetadata().put("jvm.uptime",
                                   new Metadata("jvm.uptime", MetricType.GAUGE, MetricUnits.MILLISECONDS));

  }

  private void registerGarbageCollectors(BaseMetricWorker baseMetricWorker) {
    List<Metadata> entries = baseMetricWorker.findGarbageCollectors();
    for (Metadata entry : entries) {
      baseRegistry.getMetadata().put(entry.getName(), entry);
    }
  }


  public Injector<ServerEnvironment> getServerEnvironmentInjector() {
      return this.serverEnvironmentValue;
  }

  public Injector<ModelController> getModelControllerInjector() {
      return this.modelControllerValue;
  }

}
