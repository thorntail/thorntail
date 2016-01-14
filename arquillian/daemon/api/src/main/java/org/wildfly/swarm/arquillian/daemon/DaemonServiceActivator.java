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

import org.jboss.modules.Module;
import org.jboss.modules.ModuleIdentifier;
import org.jboss.modules.ModuleLoadException;
import org.jboss.modules.ModuleLoader;
import org.jboss.msc.inject.Injector;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.ServiceActivator;
import org.jboss.msc.service.ServiceActivatorContext;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.ServiceRegistryException;
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
        DaemonService runner = new DaemonService();
        serviceActivatorContext.getServiceTarget()
                .addService(ServiceName.of("wildfly", "swarm", "arquillian", "daemon", "runner"),
                            runner)
                .addDependency(ServiceName.JBOSS.append("as", "service-module-loader"),
                               ModuleLoader.class,
                               runner.getServiceLoader())
                .setInitialMode(ServiceController.Mode.ACTIVE)
                .install();
    }

    class DaemonService implements Service<Void> {

        @Override
        public void start(StartContext context) throws StartException {
            try {
                final String artifactName = System.getProperty(BootstrapProperties.APP_ARTIFACT);
                if (artifactName == null) {
                    throw new StartException("Failed to find artifact name under " + BootstrapProperties.APP_ARTIFACT);
                }

                final ModuleLoader serviceLoader = this.serviceLoader.getValue();
                final String moduleName = "deployment." + artifactName;
                final Module module = serviceLoader.loadModule(ModuleIdentifier.create(moduleName));
                if (module == null) {
                    throw new StartException("Failed to find deployment module under " + moduleName);
                }

                //TODO: allow overriding the default port?
                this.server =  Server.create("localhost", 12345, module.getClassLoader());
                this.server.start();
            } catch (ModuleLoadException | ServerLifecycleException e) {
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

        public Injector<ModuleLoader> getServiceLoader() {
            return serviceLoader;
        }

        private Server server;
        private InjectedValue<ModuleLoader> serviceLoader = new InjectedValue<>();
    }


}
