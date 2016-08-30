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
package org.wildfly.swarm.arquillian.daemon;

import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.Phase;
import org.jboss.as.server.deployment.Services;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.ServiceActivator;
import org.jboss.msc.service.ServiceActivatorContext;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.ServiceRegistryException;
import org.jboss.msc.service.ServiceTarget;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;
import org.wildfly.swarm.arquillian.daemon.server.Server;
import org.wildfly.swarm.arquillian.daemon.server.ServerLifecycleException;
import org.wildfly.swarm.bootstrap.util.BootstrapProperties;

public class DaemonServiceActivator implements ServiceActivator {

    @Override
    public void activate(ServiceActivatorContext serviceActivatorContext) throws ServiceRegistryException {
        DaemonService.addService(serviceActivatorContext.getServiceTarget());
    }

    static class DaemonService implements Service<Void> {

        @Override
        public void start(StartContext context) throws StartException {
            try {
                DeploymentUnit depunit = injectedDeploymentUnit.getValue();

                //TODO: allow overriding the default port?
                this.server = Server.create("localhost", 12345, depunit);
                this.server.start();
            } catch (ServerLifecycleException e) {
                throw new StartException(e);
            }
        }

        @Override
        public void stop(StopContext context) {
            try {
                this.server.stop();
            } catch (ServerLifecycleException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public Void getValue() throws IllegalStateException, IllegalArgumentException {
            return null;
        }

        static void addService(ServiceTarget serviceTarget) {

            final String artifactName = System.getProperty(BootstrapProperties.APP_ARTIFACT);
            if (artifactName == null)
                throw new IllegalStateException("Failed to find artifact name under " + BootstrapProperties.APP_ARTIFACT);

            DaemonService runner = new DaemonService();
            serviceTarget
                    .addService(ServiceName.of("wildfly", "swarm", "arquillian", "daemon", "runner"), runner)
                    .addDependency(Services.deploymentUnitName(artifactName), DeploymentUnit.class, runner.injectedDeploymentUnit)
                    .addDependency(Services.deploymentUnitName(artifactName, Phase.POST_MODULE))
                    .setInitialMode(ServiceController.Mode.ACTIVE)
                    .install();
        }

        private final InjectedValue<DeploymentUnit> injectedDeploymentUnit = new InjectedValue<>();

        private Server server;
    }
}
