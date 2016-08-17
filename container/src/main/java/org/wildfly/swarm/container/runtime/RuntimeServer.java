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

import java.lang.reflect.Field;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.jboss.as.controller.ModelController;
import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.as.server.SelfContainedContainer;
import org.jboss.as.server.Services;
import org.jboss.dmr.ModelNode;
import org.jboss.modules.Module;
import org.jboss.modules.ModuleIdentifier;
import org.jboss.modules.ModuleLoadException;
import org.jboss.msc.service.ServiceActivator;
import org.jboss.msc.service.ServiceContainer;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.ValueService;
import org.jboss.msc.value.ImmediateValue;
import org.jboss.shrinkwrap.api.Archive;
import org.wildfly.swarm.bootstrap.logging.BootstrapLogger;
import org.wildfly.swarm.container.internal.Deployer;
import org.wildfly.swarm.container.internal.Server;
import org.wildfly.swarm.container.runtime.cdi.ProjectStageImpl;
import org.wildfly.swarm.container.runtime.deployments.DefaultDeploymentCreator;
import org.wildfly.swarm.container.runtime.marshal.DMRMarshaller;
import org.wildfly.swarm.container.runtime.wildfly.SimpleContentProvider;
import org.wildfly.swarm.container.runtime.wildfly.UUIDFactory;
import org.wildfly.swarm.spi.api.Customizer;
import org.wildfly.swarm.spi.api.Fraction;
import org.wildfly.swarm.spi.api.ProjectStage;
import org.wildfly.swarm.spi.api.StageConfig;
import org.wildfly.swarm.spi.runtime.annotations.Post;
import org.wildfly.swarm.spi.runtime.annotations.Pre;

/**
 * @author Bob McWhirter
 * @author Ken Finnigan
 */
@Singleton
public class RuntimeServer implements Server {

    @Inject
    @Any
    private Instance<Fraction> allFractions;

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

    private String defaultDeploymentType;


    public RuntimeServer() {
    }

    public void setXmlConfig(Optional<URL> xmlConfig) {
        this.xmlConfig = xmlConfig;
    }

    public void setStageConfig(Optional<ProjectStage> enabledConfig) {
        this.enabledStage = enabledConfig;
    }

    @Produces
    @Singleton
    public ProjectStage projectStage() {
        if (this.enabledStage.isPresent()) {
            return this.enabledStage.get();
        }

        return new ProjectStageImpl("default");
    }

    @Produces
    @Singleton
    public StageConfig stageConfig() {
        return new StageConfig(projectStage());
    }

    @Produces
    @ApplicationScoped
    ModelControllerClient client() {
        return this.client;
    }

    public void debug(boolean debug) {
        this.debug = debug;
    }

    public Deployer start(boolean eagerOpen) throws Exception {

        UUID uuid = UUIDFactory.getUUID();
        System.setProperty("jboss.server.management.uuid", uuid.toString());

        for (Customizer each : this.preCustomizers) {
            each.customize();
        }

        for (Customizer each : this.postCustomizers) {
            each.customize();
        }

        List<ModelNode> bootstrapOperations = new ArrayList<>();
        this.dmrMarshaller.marshal(bootstrapOperations);

        if (LOG.isDebugEnabled()) {
            LOG.debug(bootstrapOperations);
        }

        Thread.currentThread().setContextClassLoader(RuntimeServer.class.getClassLoader());

        List<ServiceActivator> activators = new ArrayList<>();

        this.serviceActivators.forEach(activators::add);

        this.serviceContainer = this.container.start(bootstrapOperations, this.contentProvider, activators);
        for (ServiceName serviceName : this.serviceContainer.getServiceNames()) {
            ServiceController<?> serviceController = this.serviceContainer.getService(serviceName);
            StartException exception = serviceController.getStartException();
            if (exception != null) {
                throw exception;
            }
        }

        ModelController controller = (ModelController) this.serviceContainer.getService(Services.JBOSS_SERVER_CONTROLLER).getValue();
        Executor executor = Executors.newSingleThreadExecutor();

        this.client = controller.createClient(executor);

        RuntimeDeployer deployer = this.deployer.get();

        this.serviceContainer.addService(ServiceName.of("swarm", "deployer"), new ValueService<>(new ImmediateValue<Deployer>(deployer))).install();

        setupUserSpaceExtension();

        for (Archive each : this.implicitDeployments) {
            deployer.deploy(each);
        }

        return deployer;
    }

    private void setupUserSpaceExtension() {
        try {
            Module module = Module.getBootModuleLoader().loadModule(ModuleIdentifier.create("org.wildfly.swarm.cdi", "ext"));
            Class<?> use = module.getClassLoader().loadClass("org.wildfly.swarm.cdi.InjectStageConfigExtension");
            Field field = use.getDeclaredField("stageConfig");
            field.setAccessible(true);
            field.set(null, this.stageConfig());
        } catch (ModuleLoadException e) {
            // ignore, don't do it.
        } catch (ClassNotFoundException | IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
        }

    }

    public void stop() throws Exception {
        final CountDownLatch latch = new CountDownLatch(1);
        this.serviceContainer.addTerminateListener(info -> latch.countDown());
        this.serviceContainer.shutdown();

        latch.await();

        this.deployer.get().stop();
        this.serviceContainer = null;
        this.client = null;
        this.deployer = null;
    }

    @Override
    public Deployer deployer() {
        return this.deployer.get();
    }

    private SelfContainedContainer container = new SelfContainedContainer();

    private ServiceContainer serviceContainer;

    private ModelControllerClient client;

    // optional XML config
    private Optional<URL> xmlConfig = Optional.empty();

    private BootstrapLogger LOG = BootstrapLogger.logger("org.wildfly.swarm.runtime.server");

    // TODO : still needed or merge error?
    private boolean debug;

    private Optional<ProjectStage> enabledStage = Optional.empty();

}
