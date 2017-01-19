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
package org.wildfly.swarm.container.runtime;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.jboss.as.controller.ModelController;
import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.as.controller.persistence.ExtensibleConfigurationPersister;
import org.jboss.as.server.Bootstrap;
import org.jboss.as.server.SelfContainedContainer;
import org.jboss.as.server.ServerEnvironment;
import org.jboss.as.server.Services;
import org.jboss.dmr.ModelNode;
import org.jboss.msc.service.ServiceActivator;
import org.jboss.msc.service.ServiceContainer;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.ValueService;
import org.jboss.msc.value.ImmediateValue;
import org.jboss.shrinkwrap.api.Archive;
import org.wildfly.swarm.bootstrap.logging.BootstrapLogger;
import org.wildfly.swarm.bootstrap.util.TempFileManager;
import org.wildfly.swarm.container.internal.Deployer;
import org.wildfly.swarm.container.internal.Server;
import org.wildfly.swarm.container.runtime.deployments.DefaultDeploymentCreator;
import org.wildfly.swarm.container.runtime.marshal.DMRMarshaller;
import org.wildfly.swarm.container.runtime.wildfly.SimpleContentProvider;
import org.wildfly.swarm.container.runtime.wildfly.UUIDFactory;
import org.wildfly.swarm.container.runtime.xmlconfig.BootstrapConfiguration;
import org.wildfly.swarm.container.runtime.xmlconfig.BootstrapPersister;
import org.wildfly.swarm.internal.SwarmMessages;
import org.wildfly.swarm.spi.api.Customizer;
import org.wildfly.swarm.spi.api.UserSpaceExtensionFactory;
import org.wildfly.swarm.spi.runtime.annotations.Post;
import org.wildfly.swarm.spi.runtime.annotations.Pre;

/**
 * @author Bob McWhirter
 * @author Ken Finnigan
 */
@Singleton
public class RuntimeServer implements Server {

    @Inject
    @Pre
    private Instance<Customizer> preCustomizers;

    @Inject
    @Post
    private Instance<Customizer> postCustomizers;

    @Inject
    @Any
    private Instance<ServiceActivator> serviceActivators;

    @Inject
    @Any
    private Instance<Archive> implicitDeployments;

    @Inject
    private DMRMarshaller dmrMarshaller;

    @Inject
    private DefaultDeploymentCreator defaultDeploymentCreator;

    @Inject
    private SimpleContentProvider contentProvider;

    @Inject
    private Instance<RuntimeDeployer> deployer;

    @Inject
    @Any
    private Instance<UserSpaceExtensionFactory> userSpaceExtensionFactories;

    @Inject
    private ConfigurableManager configurableManager;

    public RuntimeServer() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (container != null) {
                try {
                    LOG.info("Shutdown requested ...");
                    stop();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }));
    }

    @Produces
    @ApplicationScoped
    ModelControllerClient client() {
        return this.client;
    }

    public Deployer start(boolean eagerOpen) throws Exception {

        UUID uuid = UUIDFactory.getUUID();
        System.setProperty("jboss.server.management.uuid", uuid.toString());

        File configurationFile;
        try {
            configurationFile = TempFileManager.INSTANCE.newTempFile("swarm-config-", ".xml");
            LOG.debug("Temporarily storing configuration at: " + configurationFile.getAbsolutePath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        List<ModelNode> bootstrapOperations = new ArrayList<>();
        BootstrapConfiguration bootstrapConfiguration = () -> bootstrapOperations;

        this.container = new SelfContainedContainer(new Bootstrap.ConfigurationPersisterFactory() {
            @Override
            public ExtensibleConfigurationPersister createConfigurationPersister(ServerEnvironment serverEnvironment, ExecutorService executorService) {
                return new BootstrapPersister(bootstrapConfiguration, configurationFile);
            }
        });

        for (Customizer each : this.preCustomizers) {
            SwarmMessages.MESSAGES.callingPreCustomizer(each);
            each.customize();
        }

        for (Customizer each : this.postCustomizers) {
            SwarmMessages.MESSAGES.callingPostCustomizer(each);
            each.customize();
        }

        this.configurableManager.rescan();
        this.configurableManager.log();
        this.configurableManager.close();

        this.dmrMarshaller.marshal(bootstrapOperations);

        SwarmMessages.MESSAGES.wildflyBootstrap(bootstrapOperations.toString());

        Thread.currentThread().setContextClassLoader(RuntimeServer.class.getClassLoader());

        List<ServiceActivator> activators = new ArrayList<>();

        this.serviceActivators.forEach(activators::add);

        final ServiceContainer serviceContainer = this.container.start(bootstrapOperations, this.contentProvider, activators);
        for (ServiceName serviceName : serviceContainer.getServiceNames()) {
            ServiceController<?> serviceController = serviceContainer.getService(serviceName);
            StartException exception = serviceController.getStartException();
            if (exception != null) {
                throw exception;
            }
        }

        ModelController controller = (ModelController) serviceContainer.getService(Services.JBOSS_SERVER_CONTROLLER).getValue();
        Executor executor = Executors.newSingleThreadExecutor();

        this.client = controller.createClient(executor);

        RuntimeDeployer deployer = this.deployer.get();

        serviceContainer.addService(ServiceName.of("swarm", "deployer"), new ValueService<>(new ImmediateValue<Deployer>(deployer))).install();

        configureUserSpaceExtensions();

        for (Archive each : this.implicitDeployments) {
            deployer.deploy(each);
        }

        return deployer;
    }

    private void configureUserSpaceExtensions() {
        this.userSpaceExtensionFactories.forEach(factory -> {
            try {
                factory.configure();
            } catch (Exception e) {
                SwarmMessages.MESSAGES.errorInstallingUserSpaceExtension(factory.getClass().getName());
            }
        });
    }

    public void stop() throws Exception {
        this.container.stop();
        awaitContainerTermination();
        this.container = null;
        this.client = null;
        this.deployer = null;
    }

    private void awaitContainerTermination() {
        try {
            Field field = this.container.getClass().getDeclaredField("executor");
            field.setAccessible(true);
            ExecutorService executor = (ExecutorService) field.get(this.container);
            executor.awaitTermination(10, TimeUnit.SECONDS);
        } catch (NoSuchFieldException | IllegalAccessException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Deployer deployer() {
        return this.deployer.get();
    }

    private SelfContainedContainer container;

    private ModelControllerClient client;

    private BootstrapLogger LOG = BootstrapLogger.logger("org.wildfly.swarm.runtime.server");
}
