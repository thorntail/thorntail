/**
 * Copyright 2015-2018 Red Hat, Inc, and individual contributors.
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

import io.opentracing.contrib.jaxrs2.client.ClientTracingFeature.Builder;
import java.util.concurrent.Executors;
import javax.enterprise.inject.spi.CDI;
import org.eclipse.microprofile.opentracing.ClientTracingRegistrarProvider;
import io.opentracing.contrib.concurrent.TracedExecutorService;
import io.opentracing.Tracer;
import java.util.concurrent.ExecutorService;
import javax.ws.rs.client.ClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;

/**
 * @author Pavol Loffay
 */
public class ResteasyClientTracingRegistrarProvider implements ClientTracingRegistrarProvider {

  public ClientBuilder configure(ClientBuilder clientBuilder) {
    // Make sure executor is the same as a default in resteasy ClientBuilder
    return configure(clientBuilder, Executors.newFixedThreadPool(10));
  }

  public ClientBuilder configure(ClientBuilder clientBuilder, ExecutorService executorService) {
    ResteasyClientBuilder resteasyClientBuilder = (ResteasyClientBuilder)clientBuilder;
    Tracer tracer = CDI.current().select(Tracer.class).get();
    return resteasyClientBuilder.asyncExecutor(new TracedExecutorService(executorService, tracer))
      .register(new Builder(tracer)
          .withTraceSerialization(false)
          .build());
  }
}
