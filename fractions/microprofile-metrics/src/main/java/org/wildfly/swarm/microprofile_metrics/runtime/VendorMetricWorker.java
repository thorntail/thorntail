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

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ADDRESS;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.INCLUDE_RUNTIME;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OUTCOME;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.READ_RESOURCE_OPERATION;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.RECURSIVE;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.RECURSIVE_DEPTH;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.RESULT;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.SUCCESS;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.microprofile.metrics.Metadata;
import org.eclipse.microprofile.metrics.MetricRegistry;
import org.eclipse.microprofile.metrics.MetricType;
import org.eclipse.microprofile.metrics.MetricUnits;
import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.dmr.ModelNode;

/**
 * @author hrupp
 */
public class VendorMetricWorker {
  private static final String CORE_SERVICE = "core-service";
  private static final String PLATFORM_MBEAN = "platform-mbean";
  private static final String TYPE = "type";
  private static final String QUERY = "query";
  private static final String MEMORY_POOL = "memoryPool.";
  private static VendorMetricWorker worker;
  private ModelControllerClient controllerClient;

  private VendorMetricWorker() { /* Singleton */ }

  public static VendorMetricWorker create(ModelControllerClient controllerClient) {

    worker = new VendorMetricWorker();
    worker.controllerClient = controllerClient;
    return worker;
  }

  public static VendorMetricWorker instance() {
    if (worker == null) {
      throw new IllegalStateException("You must first create a worker via create()");
    }
    return worker;
  }

   Map<String,Double> getVendorMetrics() {

     Map<String,Double> outcome = new HashMap<>();

     // Get the memory pools
    ModelNode op = new ModelNode();

     op.get(ADDRESS).add(CORE_SERVICE, PLATFORM_MBEAN);
     op.get(ADDRESS).add(TYPE, "memory-pool");
     op.get(OP).set(READ_RESOURCE_OPERATION);
     op.get(INCLUDE_RUNTIME).set(true);
     op.get(RECURSIVE).set(true);
     op.get(RECURSIVE_DEPTH).set(1); // If we don't set this, depth=0

     try {
       ModelNode response = controllerClient.execute(op);
       ModelNode result = unwrap(response);


       List<ModelNode> collectors = result.get("name").asList();

       for (ModelNode node : collectors) {
         String collectorName = node.asProperty().getName();
         String baseName = MEMORY_POOL + collectorName;

         outcome.put(baseName + ".usage", node.asProperty().getValue().get("usage").get("used").asDouble());
         outcome.put(baseName + ".usage.max", node.asProperty().getValue().get("peak-usage").get("used").asDouble());

       }
     } catch (IOException e) {
       throw new RuntimeException(e); // TODO return some 500 message or such
     }

     return outcome;
   }


  private static ModelNode unwrap(ModelNode response) {
      if (response.get(OUTCOME).asString().equals(SUCCESS)) {
          return response.get(RESULT);
      } else {
          return response;
      }
  }

  void registerVendorMetrics() {

    MetricRegistry vendorRegistry = MetricRegistryFactory.get(MetricRegistry.Type.VENDOR);

    // baseRegistry.getMetadata().put("thread.count", new Metadata("thread.count", MetricType.COUNTER));
    List<Metadata> entries = findMemoryPools();
    for (Metadata entry : entries) {
      vendorRegistry.getMetadata().put(entry.getName(), entry);
    }

  }

  public List<Metadata> findMemoryPools() {

    ModelNode op = new ModelNode();
    op.get(ADDRESS).add(CORE_SERVICE, PLATFORM_MBEAN);
    op.get(ADDRESS).add(TYPE, "memory-pool");
    op.get(OP).set(QUERY);

    try {
        ModelNode response = controllerClient.execute(op);
        ModelNode result = unwrap(response);

        List<ModelNode> collectors = result.get("name").asList();
        List<Metadata> output = new ArrayList<>(2 * collectors.size());
        for (ModelNode node : collectors) {
          String collectorName = node.asProperty().getName();
          String baseName = MEMORY_POOL + collectorName;
          Metadata m = new Metadata(baseName + ".usage", MetricType.GAUGE, MetricUnits.BYTES);
          output.add(m);
          m = new Metadata(baseName + ".usage.max", MetricType.GAUGE, MetricUnits.BYTES);
          output.add(m);
        }

        return output;

    } catch (IOException e) {
        throw new RuntimeException(e); // TODO return some 500 message or such
    }

  }
}
