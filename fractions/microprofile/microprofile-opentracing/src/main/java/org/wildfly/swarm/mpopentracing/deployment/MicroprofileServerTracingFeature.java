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

import io.opentracing.Tracer;
import io.opentracing.contrib.jaxrs2.server.OperationNameProvider.ClassNameOperationName;
import io.opentracing.contrib.jaxrs2.server.ServerTracingDynamicFeature;
import io.opentracing.contrib.jaxrs2.server.ServerTracingDynamicFeature.Builder;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.CDI;
import javax.ws.rs.container.DynamicFeature;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.ext.Provider;

/**
 * @author Pavol Loffay
 */
@Provider
public class MicroprofileServerTracingFeature implements DynamicFeature {

  private ServerTracingDynamicFeature delegate;

  public MicroprofileServerTracingFeature() {
    Instance<Tracer> tracerInstance = CDI.current().select(Tracer.class);
    this.delegate = new Builder(tracerInstance.get())
        .withOperationNameProvider(ClassNameOperationName.newBuilder())
        .withTraceSerialization(false)
        .build();
  }

  @Override
  public void configure(ResourceInfo resourceInfo, FeatureContext context) {
    delegate.configure(resourceInfo, context);
  }
}
