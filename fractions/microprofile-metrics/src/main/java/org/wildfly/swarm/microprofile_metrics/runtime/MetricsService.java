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

import org.eclipse.microprofile.metrics.Metric;
import org.jboss.as.controller.ModelController;
import org.jboss.as.server.ServerEnvironment;
import org.jboss.logging.Logger;
import org.jboss.msc.inject.Injector;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;
import org.wildfly.swarm.microprofile_metrics.runtime.mbean.MCounterImpl;
import org.wildfly.swarm.microprofile_metrics.runtime.mbean.MGaugeImpl;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Heiko W. Rupp
 */
public class MetricsService implements Service<MetricsService> {

  private static Logger LOG = Logger.getLogger("org.wildfly.swarm.microprofile_metrics");

  public static final ServiceName SERVICE_NAME = ServiceName.of("swarm", "mp-metrics");

  private final InjectedValue<ServerEnvironment> serverEnvironmentValue = new InjectedValue<ServerEnvironment>();
  private final InjectedValue<ModelController> modelControllerValue = new InjectedValue<ModelController>();


  @Override
  public void start(StartContext context) throws StartException {
    initBaseAndVendorConfiguration();


    LOG.info("MicroProfile-Metrics started");
  }

  /**
   * Read a list of mappings that contains the base and vendor metrics
   * along with their metadata.
   */
  private void initBaseAndVendorConfiguration() {
    InputStream is  = getClass().getResourceAsStream("mapping.yml");

    if (is != null) {
      ConfigReader cr = new ConfigReader();
      MetadataList ml = cr.readConfig(is);

      String globalTagsFromEnv = System.getenv("MP_METRICS_TAGS");
      List<Tag> globalTags = convertToTags(globalTagsFromEnv);

      // Turn the multi-entry query expressions into concrete entries.
      JmxWorker.instance().expandMultiValueEntries(ml.getBase());
      JmxWorker.instance().expandMultiValueEntries(ml.getVendor());

      for (ExtendedMetadata em : ml.getBase()) {
        em.processTags(globalTags);
        Metric type = getType(em);
        MetricRegistryFactory.getBaseRegistry().register(em.getName(),type,em);
      }
      for (ExtendedMetadata em : ml.getVendor()) {
        em.processTags(globalTags);
        Metric type = getType(em);
        MetricRegistryFactory.getVendorRegistry().register(em.getName(),type,em);
      }
    } else {
      throw new IllegalStateException("Was not able to find the mapping file 'mapping.yml'");
    }
  }

  private Metric getType(ExtendedMetadata em) {
    Metric out;
    switch (em.getTypeRaw()) {
      case GAUGE:
        out = new MGaugeImpl(em.getMbean());
        break;
      case COUNTER:
        out = new MCounterImpl(em.getMbean());
        break;
      default:
        throw new IllegalStateException("Not yet supported: " + em);
    }
    return out;
  }

  private List<Tag> convertToTags(String globalTagsString) {
    List<Tag> tags = new ArrayList<>();
    String[] singleTags = globalTagsString.split(",");
        for (String singleTag : singleTags) {
          tags.add(new Tag(singleTag.trim()));
        }
    return tags;
  }

  @Override
  public void stop(StopContext context) {
  }

  @Override
  public MetricsService getValue() throws IllegalStateException, IllegalArgumentException {
    return this;
  }

  /**
   * Register the metrics of the base scope with the system.
   */




  public Injector<ServerEnvironment> getServerEnvironmentInjector() {
      return this.serverEnvironmentValue;
  }

  public Injector<ModelController> getModelControllerInjector() {
      return this.modelControllerValue;
  }

}
