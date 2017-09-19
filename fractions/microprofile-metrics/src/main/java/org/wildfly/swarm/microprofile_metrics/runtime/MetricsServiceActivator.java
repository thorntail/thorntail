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

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import org.jboss.as.controller.ModelController;
import org.jboss.as.server.ServerEnvironment;
import org.jboss.as.server.ServerEnvironmentService;
import org.jboss.as.server.Services;
import org.jboss.msc.service.ServiceActivator;
import org.jboss.msc.service.ServiceActivatorContext;
import org.jboss.msc.service.ServiceBuilder;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceRegistryException;
import org.jboss.msc.service.ServiceTarget;
import org.wildfly.swarm.microprofile_metrics.MicroprofileMetricsFraction;

/**
 * @author hrupp
 */
@ApplicationScoped
public class MetricsServiceActivator implements ServiceActivator {

  @Inject
  @Any
  Instance<MicroprofileMetricsFraction> fractionInstance;

  @Override
  public void activate(ServiceActivatorContext serviceActivatorContext) throws ServiceRegistryException {

    ServiceTarget target = serviceActivatorContext.getServiceTarget();

    MetricsService service = new MetricsService();
    ServiceBuilder<MetricsService> metricsServiceBuilder = target.addService(MetricsService.SERVICE_NAME,service);

    ServiceBuilder<MetricsService> serviceBuilder = metricsServiceBuilder
            .addDependency(ServerEnvironmentService.SERVICE_NAME, ServerEnvironment.class, service.getServerEnvironmentInjector())
            .addDependency(Services.JBOSS_SERVER_CONTROLLER, ModelController.class, service.getModelControllerInjector());

    serviceBuilder.setInitialMode(ServiceController.Mode.ACTIVE)
             .install();
  }
}
