/**
 * Copyright 2015-2016 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wildfly.swarm.opentracing.tracer.deployment;

import io.opentracing.Tracer;
import io.opentracing.contrib.tracerresolver.TracerResolver;
import io.opentracing.util.GlobalTracer;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import org.jboss.logging.Logger;

/**
 * @author Pavol Loffay
 */
public class TracerResolverListener implements ServletContextListener {
  private static final Logger logger = Logger.getLogger(TracerResolverListener.class);

  @Override
  public void contextInitialized(ServletContextEvent sce) {
    ServletContext servletContext = sce.getServletContext();

    String skipParameter = servletContext.getInitParameter("skipOpenTracingResolver");
    if (skipParameter != null && Boolean.parseBoolean(skipParameter)) {
      logger.info("Skipping the OpenTracing TracerResolver. "
          + "Your application is expected to set a tracer to GlobalTracer explicitly.");
      return;
    }
    if (GlobalTracer.isRegistered()) {
      logger.info("A Tracer is already registered at the GlobalTracer. Skipping resolution via TraceResolver.");
    }

    Tracer tracer = TracerResolver.resolveTracer();
    if (tracer != null) {
      logger.info(String.format("Registering resolved tracer %s to GlobalTracer.",
          tracer.getClass().getCanonicalName()));
      GlobalTracer.register(tracer);
    } else {
      logger.info("No Tracerresolver found on classpath!");
    }
  }

  @Override
  public void contextDestroyed(ServletContextEvent sce) {
  }
}
