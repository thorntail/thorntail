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
import io.undertow.util.HttpString;
import org.eclipse.microprofile.metrics.MetricRegistry;
import org.jboss.logging.Logger;
import org.wildfly.swarm.microprofile_metrics.runtime.exporters.Exporter;
import org.wildfly.swarm.microprofile_metrics.runtime.exporters.JsonExporter;
import org.wildfly.swarm.microprofile_metrics.runtime.exporters.JsonMetadataExporter;
import org.wildfly.swarm.microprofile_metrics.runtime.exporters.PrometheusExporter;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

/**
 * @author hrupp
 */
@SuppressWarnings("unused")
public class MetricsHttpHandler implements HttpHandler {

  private static Logger LOG = Logger.getLogger("org.wildfly.swarm.microprofile.metrics");
  protected ThreadLocal<CountDownLatch> dispatched = new ThreadLocal<>();



  private HttpHandler next;

  public MetricsHttpHandler(HttpHandler next) {

    this.next = next;
  }

  @Override
  public void handleRequest(HttpServerExchange exchange) throws Exception {

    String requestPath = exchange.getRequestPath();
//    LOG.warn(requestPath + " on " + Thread.currentThread());

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

    Exporter exporter = obtainExporter(exchange);
//    LOG.warn("scope path >" + scopePath + "< and exporter " + exporter.getClass().getName());

    if (scopePath.startsWith("/")) {
      scopePath = scopePath.substring(1);
    }

    StringBuilder sb;

    if (scopePath.isEmpty()) {
      Map<MetricRegistry.Type,Map<String,Double>> metricValuesMap = new HashMap<>();
      for (MetricRegistry.Type scope  :  MetricRegistry.Type.values()) {
        Map<String, Double> map = getMetricsMapForScope(scope);
        metricValuesMap.put(scope, map);
      }
      sb = exporter.exportAllScopes();

    } else if (scopePath.contains("/")) {
      // One metric in a scope

      String attribute = scopePath.substring(scopePath.indexOf('/') + 1);

      MetricRegistry.Type scope = getScopeFromPath(exchange, scopePath.substring(0, scopePath.indexOf('/')));
      Map<String, Double> metricValuesMap = getMetricsMapForScope(scope);

      Map<String,Double> oneMetric = new HashMap<>(1);
      oneMetric.put(attribute, metricValuesMap.get(attribute));

      sb = exporter.exportOneMetric(scope,attribute);


    } else {
      // A single scope

      MetricRegistry.Type scope = getScopeFromPath(exchange, scopePath);
      if (scope == null) {
        return;
      }

      MetricRegistry reg = MetricRegistryFactory.get(scope);
      if (reg.getMetadata().size() == 0) {
        exchange.setStatusCode(204);
        exchange.setReasonPhrase("No data in scope " + scopePath);
      }

      Map<String, Double> metricValuesMap = getMetricsMapForScope(scope);

      sb = exporter.exportOneScope(scope);
    }

    if (requestPath.contains("app") && exchange.getRequestMethod().toString().equals("GET")) {
      LOG.info("Sending:-----------\n" + sb.toString() + "\n-------------");
    }
    exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, exporter.getContentType());
    exchange.getResponseHeaders().put(new HttpString("Access-Control-Allow-Origin"), "*");
    exchange.getResponseHeaders().put(new HttpString("Access-Control-Allow-Headers"), "origin, content-type, accept, authorization");
    exchange.getResponseHeaders().put(new HttpString("Access-Control-Allow-Credentials"), "true");
    exchange.getResponseHeaders().put(new HttpString("Access-Control-Allow-Methods"), "GET, POST, PUT, DELETE, OPTIONS, HEAD");
    exchange.getResponseHeaders().put(new HttpString("Access-Control-Max-Age"), "1209600");
    exchange.getResponseSender().send(sb.toString());

  }

  private MetricRegistry.Type getScopeFromPath(HttpServerExchange exchange, String scopePath) {
    MetricRegistry.Type scope;
    try {
      scope = MetricRegistry.Type.valueOf(scopePath.toUpperCase());
    } catch (IllegalArgumentException iae) {
      exchange.setStatusCode(404);
      exchange.setReasonPhrase("Bad scope requested: " + scopePath);
      return null;
    }
    return scope;
  }

  private Map<String, Double> getMetricsMapForScope(MetricRegistry.Type scope) {
    Map<String, Double> metricValuesMap;
/*
    if (scope.equals(MetricRegistry.Type.BASE)) {
      metricValuesMap = BaseMetricWorker.instance().getBaseMetrics();
    } else if (scope.equals(MetricRegistry.Type.VENDOR)) {
      metricValuesMap = VendorMetricWorker.instance().getVendorMetrics();
*/
    if (scope.equals(MetricRegistry.Type.BASE) || scope.equals(MetricRegistry.Type.VENDOR)) {
      metricValuesMap = JmxWorker.instance().getMetrics(scope);
    } else {
      metricValuesMap = new HashMap<>(); // TODO
    }
    return metricValuesMap;
  }

  private Exporter obtainExporter(HttpServerExchange exchange) {
    HeaderValues acceptHeaders = exchange.getRequestHeaders().get(Headers.ACCEPT);
    Exporter exporter;

    if (acceptHeaders == null) {
      exporter = new PrometheusExporter();
    } else {
      // Header can look like "application/json, text/plain, */*"
      if (acceptHeaders.getFirst() != null && acceptHeaders.getFirst().startsWith("application/json")) {

        String method = exchange.getRequestMethod().toString();
        if (method.equals("GET")) {
          exporter = new JsonExporter();
        } else if (method.equals("OPTIONS")) {
          exporter = new JsonMetadataExporter();
        } else {
          throw new IllegalStateException("Unsupported method");
        }
      } else {
        // This is the fallback
        exporter = new PrometheusExporter();
      }
    }
    return exporter;
  }


}
