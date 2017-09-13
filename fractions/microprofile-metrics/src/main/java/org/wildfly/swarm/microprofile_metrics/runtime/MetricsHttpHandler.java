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

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import org.eclipse.microprofile.metrics.Metadata;
import org.eclipse.microprofile.metrics.MetricRegistry;
import org.jboss.logging.Logger;

/**
 * @author hrupp
 */
public class MetricsHttpHandler implements HttpHandler {

  private static Logger LOG = Logger.getLogger("org.wildfly.swarm.microprofile.health");
  protected ThreadLocal<CountDownLatch> dispatched = new ThreadLocal<>();



  private HttpHandler next;

  public MetricsHttpHandler(HttpHandler next) {

    this.next = next;
    LOG.warn("MetricsHttpHandler()");
  }

  @Override
  public void handleRequest(HttpServerExchange exchange) throws Exception {

    LOG.warn(exchange.getRequestPath() +" on "+Thread.currentThread());

    if (dispatched.get() != null && dispatched.get().getCount() == 1) {
        next.handleRequest(exchange);
        dispatched.set(null);
        return;
    }

    if (!exchange.getRequestPath().startsWith("/metrics")) {
      next.handleRequest(exchange);
      return;
    }

    if (exchange.getRequestPath().startsWith("/metrics/base")) {
      StringBuilder sb = new StringBuilder();

      Map<String,Double> baseMetricsMap = BaseMetricWorker.instance().getBaseMetrics();
      MetricRegistry baseRegistry = MetricRegistryFactory.getBaseRegistry();

      for (Map.Entry<String,Double> entry: baseMetricsMap.entrySet()) {
        String key = entry.getKey();
        Metadata md = baseRegistry.getMetadata().get(key);

        key = key.replace('-', '_').replace('.', '_').replace(' ','_');
        sb.append("# TYPE ").append("base:").append(key).append(" ").append(md.getType()).append("\n");
        sb.append("base:").append(key).append(" ").append(entry.getValue()).append("\n");
      }


      exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
      exchange.getResponseSender().send(sb.toString());
    }
    else {
      exchange.setStatusCode(404);
      exchange.setReasonPhrase("Only base metrics are supported at the moment");
    }


  }
}
