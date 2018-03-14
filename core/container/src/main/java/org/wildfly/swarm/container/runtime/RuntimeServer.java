/**
 * Copyright 2015-2017 Red Hat, Inc, and individual contributors.
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
import java.util.Set;
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

import org.jboss.as.controller.ModelController;
import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.as.controller.persistence.ExtensibleConfigurationPersister;
import org.jboss.as.server.Bootstrap;
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
import org.wildfly.swarm.bootstrap.modules.MavenResolvers;
import org.wildfly.swarm.bootstrap.performance.Performance;
import org.wildfly.swarm.bootstrap.util.JarFileManager;
import org.wildfly.swarm.bootstrap.util.TempFileManager;

import org.wildfly.swarm.container.internal.Deployer;
import org.wildfly.swarm.container.internal.Server;
import org.wildfly.swarm.container.runtime.deployments.DefaultDeploymentCreator;
import org.wildfly.swarm.container.runtime.marshal.DMRMarshaller;
import org.wildfly.swarm.container.runtime.usage.UsageCreator;
import org.wildfly.swarm.container.runtime.wildfly.ContentRepositoryServiceActivator;
import org.wildfly.swarm.container.runtime.wildfly.SwarmContentRepository;
import org.wildfly.swarm.container.runtime.wildfly.UUIDFactory;
import org.wildfly.swarm.container.runtime.xmlconfig.BootstrapConfiguration;
import org.wildfly.swarm.container.runtime.xmlconfig.BootstrapPersister;
import org.wildfly.swarm.internal.SwarmMessages;
import org.wildfly.swarm.internal.wildfly.SelfContainedContainer;
import org.wildfly.swarm.spi.api.Customizer;
import org.wildfly.swarm.spi.api.UserSpaceExtensionFactory;
import org.wildfly.swarm.spi.runtime.annotations.Post;
import org.wildfly.swarm.spi.runtime.annotations.Pre;

/**
 * @author Bob McWhirter
 * @author Ken Finnigan
 */
@ApplicationScoped
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
    @ImplicitDeployment
    private Instance<Archive> implicitDeployments;

    @Inject
    private DMRMarshaller dmrMarshaller;

    @Inject
    private DefaultDeploymentCreator defaultDeploymentCreator;

    @Inject
    private SwarmContentRepository contentRepository;

    @Inject
    private Instance<RuntimeDeployer> deployer;

    @Inject
    @Any
    private Instance<UserSpaceExtensionFactory> userSpaceExtensionFactories;

    @Inject
    private ConfigurableManager configurableManager;

    @Inject
    private UsageCreator usageCreator;

    public RuntimeServer() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (containerStarted) {
                try {
                    SwarmMessages.MESSAGES.shutdownRequested();
                    stop();
                } catch (Exception e) {
                    throw new RuntimeException(e);
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
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        this.networkConfigurer.configure();

        List<ModelNode> bootstrapOperations = new ArrayList<>();
        BootstrapConfiguration bootstrapConfiguration = () -> bootstrapOperations;

        this.container = new SelfContainedContainer(new Bootstrap.ConfigurationPersisterFactory() {
            @Override
            public ExtensibleConfigurationPersister createConfigurationPersister(ServerEnvironment serverEnvironment, ExecutorService executorService) {
                return new BootstrapPersister(bootstrapConfiguration, configurationFile);
            }
        });

        try (AutoCloseable handle = Performance.time("pre-customizers")) {
            for (Customizer each : this.preCustomizers) {
                SwarmMessages.MESSAGES.callingPreCustomizer(each);
                each.customize();
            }
        }

        try (AutoCloseable handle = Performance.time("post-customizers")) {
            for (Customizer each : this.postCustomizers) {
                SwarmMessages.MESSAGES.callingPostCustomizer(each);
                each.customize();
            }
        }

        this.networkConfigurer.configure();

        /*
        this.archivePreparers.forEach(e -> {
            // Log it to prevent dead-code elimination.
            //
            // This is purely to ensure @Configurables are scanned
            // prior to logging the configurables.
            SwarmMessages.MESSAGES.registeredArchivePreparer(e.toString());
        });
        */

        try (AutoCloseable handle = Performance.time("configurable-manager rescan")) {
            this.configurableManager.rescan();
            this.configurableManager.log();
        }

        try (AutoCloseable handle = Performance.time("marshall DMR")) {
            this.dmrMarshaller.marshal(bootstrapOperations);
        }

        SwarmMessages.MESSAGES.wildflyBootstrap(bootstrapOperations.toString());

        Thread.currentThread().setContextClassLoader(RuntimeServer.class.getClassLoader());

        List<ServiceActivator> activators = new ArrayList<>();

        this.serviceActivators.forEach(activators::add);

        activators.add(new ContentRepositoryServiceActivator(this.contentRepository));

        try (AutoCloseable wildflyStart = Performance.time("WildFly start")) {
            ServiceContainer serviceContainer = null;
            try (AutoCloseable startWildflyItself = Performance.time("Starting WildFly itself")) {
                //serviceContainer = this.container.start(bootstrapOperations, this.contentProvider, activators);
                serviceContainer = this.container.start(bootstrapOperations, activators);
                this.containerStarted = true;
            }
            try (AutoCloseable checkFailedServices = Performance.time("Checking for failed services")) {
                for (ServiceName serviceName : serviceContainer.getServiceNames()) {
                    ServiceController<?> serviceController = serviceContainer.getService(serviceName);
                    /*
                    if (serviceController.getImmediateUnavailableDependencies().size() > 0) {
                        System.err.println("Service missing dependencies: " + serviceController.getName());
                        for (ServiceName name : serviceController.getImmediateUnavailableDependencies()) {
                            System.err.println("  - " + name);
                        }
                    }
                    */
                    StartException exception = serviceController.getStartException();
                    if (exception != null) {
                        throw exception;
                    }
                }
            }

            /*
            for (ServiceName serviceName : serviceContainer.getServiceNames()) {
                System.err.println(" === " + serviceName);
            }
            */

            ModelController controller = (ModelController) serviceContainer.getService(Services.JBOSS_SERVER_CONTROLLER).getValue();
            Executor executor = Executors.newSingleThreadExecutor();

            try (AutoCloseable creatingControllerClient = Performance.time("Creating controller client")) {
                // Can't use controller.getClientFactory() due to non-public nature of that method.
                //noinspection deprecation
                this.client = controller.createClient(executor);
            }

            RuntimeDeployer deployer = this.deployer.get();

            try (AutoCloseable installDeployer = Performance.time("Installing deployer")) {
                serviceContainer.addService(ServiceName.of("swarm", "deployer"), new ValueService<>(new ImmediateValue<Deployer>(deployer))).install();
            }

            try (AutoCloseable configUserSpaceExt = Performance.time("Configure user-space extensions")) {
                configureUserSpaceExtensions();
            }

            try (AutoCloseable deployments = Performance.time("Implicit Deployments")) {
                for (Archive each : this.implicitDeployments) {
                    if (each != null) {
                        deployer.deploy(each);
                    }
                }
            }

            this.artifactDeployer.deploy();

            deployer.implicitDeploymentsComplete();

            return deployer;
        }
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
        this.containerStarted = false;

        //Clear the container ShutdownHook so it doesn't try to execute after container is stopped
        Field field = this.container.getClass().getDeclaredField("serviceContainer");
        field.setAccessible(true);
        ServiceContainer serviceContainer = (ServiceContainer) field.get(this.container);

        Class<?> shutdownHookHolder = null;
        Class<?>[] declaredClasses = serviceContainer.getClass().getDeclaredClasses();
        for (Class<?> clazz : declaredClasses) {
            if (clazz.getName().contains("ShutdownHookHolder")) {
                shutdownHookHolder = clazz;
            }
        }

        if (shutdownHookHolder != null) {
            Field containersSetField = shutdownHookHolder.getDeclaredField("containers");
            containersSetField.setAccessible(true);
            Set<?> set = (Set<?>)containersSetField.get(null);
            set.clear();
        }

        this.container = null;

        this.client = null;
        this.deployer.get().removeAllContent();
        this.deployer = null;

        JarFileManager.INSTANCE.close();
        TempFileManager.INSTANCE.close();
        MavenResolvers.close();
    }

    private void awaitContainerTermination() {
        try {
            Field field = this.container.getClass().getDeclaredField("executor");
            field.setAccessible(true);
            ExecutorService executor = (ExecutorService) field.get(this.container);
            executor.awaitTermination(10, TimeUnit.SECONDS);
        } catch (NoSuchFieldException | IllegalAccessException | InterruptedException e) {
            SwarmMessages.MESSAGES.errorWaitingForContainerShutdown(e);
        }
    }

    @Override
    public Deployer deployer() {
        return this.deployer.get();
    }

    @Override
    public void displayUsage() throws Exception {
        String message = this.usageCreator.getUsageMessage();
        if (message != null) {
            SwarmMessages.MESSAGES.usage(message);
        }

        this.configurableManager.close();
    }

    private SelfContainedContainer container;

    // Container does not expose this state and it's class is final so it cannot be subclassed.
    private boolean containerStarted;

    @Inject
    private ArtifactDeployer artifactDeployer;

    @Inject
    private NetworkConfigurer networkConfigurer;

    private ModelControllerClient client;

}
