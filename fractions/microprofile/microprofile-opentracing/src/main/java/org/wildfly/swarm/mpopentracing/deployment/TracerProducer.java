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

package org.wildfly.swarm.mpopentracing.deployment;

import io.opentracing.Tracer;
import io.opentracing.contrib.tracerresolver.TracerResolver;
import io.opentracing.util.GlobalTracer;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.Produces;
import javax.inject.Singleton;
import org.jboss.logging.Logger;

/**
 * @author Pavol Loffay
 */
@ApplicationScoped
public class TracerProducer {
  private static final Logger logger = Logger.getLogger(TracerProducer.class);

  /**
   * Resolves tracer instance to be used. It is using {@link TracerResolver} service loader to find
   * the tracer. It tracer is not resolved it will use {@link io.opentracing.noop.NoopTracer}.
   *
   * @return tracer instance
   */
  @Default
  @Produces
  @Singleton
  public Tracer produceTracer() {
    Tracer tracer = TracerResolver.resolveTracer();
    if (tracer == null) {
      logger.info("Could not get a valid OpenTracing Tracer from the classpath. Deferring to GlobalTracer");
      tracer = GlobalTracer.get();
    }

    logger.info(String.format("Registering %s as the OpenTracing Tracer", tracer.getClass().getName()));
    GlobalTracer.register(tracer);
    return tracer;
  }
}
