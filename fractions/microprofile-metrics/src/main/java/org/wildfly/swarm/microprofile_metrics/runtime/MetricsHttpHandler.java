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
import io.undertow.util.HeaderValues;
import io.undertow.util.Headers;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import org.eclipse.microprofile.metrics.MetricRegistry;
import org.jboss.logging.Logger;
import org.wildfly.swarm.microprofile_metrics.runtime.exporters.Exporter;
import org.wildfly.swarm.microprofile_metrics.runtime.exporters.JsonExporter;
import org.wildfly.swarm.microprofile_metrics.runtime.exporters.PrometheusExporter;

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

    String requestPath = exchange.getRequestPath();
    LOG.warn(requestPath +" on "+Thread.currentThread());

    if (dispatched.get() != null && dispatched.get().getCount() == 1) {
        next.handleRequest(exchange);
        dispatched.set(null);
        return;
    }

    if (!requestPath.startsWith("/metrics")) {
      next.handleRequest(exchange);
      return;
    }

    String scopePath = requestPath.substring(8);
    LOG.warn("scope path >" + scopePath + "<");

    Exporter exporter = obtainExporter(exchange);

    if (scopePath.startsWith("/")) {
      scopePath=scopePath.substring(1);
    }

    StringBuilder sb ;

    if (scopePath.isEmpty()) {
      Map<MetricRegistry.Type,Map<String,Double>> metricValuesMap = new HashMap<>();
      for (MetricRegistry.Type scope  :  MetricRegistry.Type.values() ) {
        Map<String,Double> map;
        if (scope.equals(MetricRegistry.Type.BASE)) {
          map = BaseMetricWorker.instance().getBaseMetrics();
        } else {
          map = new HashMap<>(); // TODO
        }
        metricValuesMap.put(scope,map);
      }
      sb = exporter.exportAllScopes(metricValuesMap);
    }
    else if (scopePath.contains("/")) {
      // TODO exportOneScope single metric
      sb = new StringBuilder("TODO");
    } else {

      MetricRegistry.Type scope;
      try {
        scope = MetricRegistry.Type.valueOf(scopePath.toUpperCase());
      } catch (IllegalArgumentException iae) {
        exchange.setStatusCode(404);
        exchange.setReasonPhrase("Bad scope requested: " + scopePath);
        return;
      }


      Map<String, Double> metricValuesMap;
      if (scope.equals(MetricRegistry.Type.BASE)) {
        metricValuesMap = BaseMetricWorker.instance().getBaseMetrics();
      } else {
        metricValuesMap = new HashMap<>(); // TODO
      }

      sb = exporter.exportOneScope(scope, metricValuesMap);
    }
    exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, exporter.getContentType());
    exchange.getResponseSender().send(sb.toString());

  }

  private Exporter obtainExporter(HttpServerExchange exchange) {
    HeaderValues acceptHeaders = exchange.getRequestHeaders().get(Headers.ACCEPT);
    Exporter exporter ;

    if (acceptHeaders==null) {
      exporter = new PrometheusExporter();
    } else {
      if (acceptHeaders.getFirst() != null && acceptHeaders.getFirst().equals("application/json")) {
        exporter = new JsonExporter();
      } else {
        // This is the fallback
        exporter = new PrometheusExporter();
      }
    }
    return exporter;
  }


}
