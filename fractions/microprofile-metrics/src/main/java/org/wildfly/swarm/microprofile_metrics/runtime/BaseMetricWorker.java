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
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OUTCOME;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.RESULT;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.SUCCESS;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.dmr.ModelNode;

/**
 * @author hrupp
 */
public class BaseMetricWorker {
  private static BaseMetricWorker worker;
  private ModelControllerClient controllerClient;

  private static final String SELECT = "select";


  private BaseMetricWorker() { /* Singleton */ }

  public static BaseMetricWorker get(ModelControllerClient controllerClient) {

    worker = new BaseMetricWorker();
    worker.controllerClient = controllerClient;
    return worker;
  }

  public static BaseMetricWorker instance() {
    if (worker == null) {
      throw new IllegalStateException("You must first create a worker via get()");
    }
    return worker;
  }

   Map<String,Double> getBaseMetrics() {

    ModelNode op = new ModelNode();
    op.get(ADDRESS).add("core-service", "platform-mbean");
    op.get(ADDRESS).add("type", "threading");
    op.get(OP).set("query");
    op.get(SELECT).add("thread-count");
    op.get(SELECT).add("peak-thread-count");
    op.get(SELECT).add("total-started-thread-count");

    try {
        ModelNode response = controllerClient.execute(op);
        ModelNode result =  unwrap(response);

        Map<String,Double> outcome = new HashMap<>();
        outcome.put("thread.count",result.get("thread-count").asDouble());
        outcome.put("thread.max.count",result.get("peak-thread-count").asDouble());

        return outcome;
    } catch (IOException e) {
        throw new RuntimeException(e); // TODO return some 500 message or such
    }


  }


  private static ModelNode unwrap(ModelNode response) {
      if (response.get(OUTCOME).asString().equals(SUCCESS)) {
          return response.get(RESULT);
      } else {
          return response;
      }
  }

}
