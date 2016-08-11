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
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Vetoed;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.xml.namespace.QName;

import org.jboss.as.controller.ModelController;
import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.as.server.SelfContainedContainer;
import org.jboss.as.server.Services;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.ModelType;
import org.jboss.dmr.Property;
import org.jboss.dmr.ValueExpression;
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
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.staxmapper.XMLElementReader;
import org.jboss.vfs.TempFileProvider;
import org.wildfly.swarm.bootstrap.logging.BootstrapLogger;
import org.wildfly.swarm.bootstrap.util.BootstrapProperties;
import org.wildfly.swarm.bootstrap.util.TempFileManager;
import org.wildfly.swarm.container.Container;
import org.wildfly.swarm.container.DeploymentException;
import org.wildfly.swarm.container.Interface;
import org.wildfly.swarm.container.internal.Deployer;
import org.wildfly.swarm.container.internal.Server;
import org.wildfly.swarm.container.runtime.internal.marshal.DMRMarshaller;
import org.wildfly.swarm.container.runtime.internal.xmlconfig.StandaloneXMLParser;
import org.wildfly.swarm.internal.FileSystemLayout;
import org.wildfly.swarm.spi.api.ArchiveMetadataProcessor;
import org.wildfly.swarm.spi.api.ArchivePreparer;
import org.wildfly.swarm.spi.api.Customizer;
import org.wildfly.swarm.spi.api.DefaultDeploymentFactory;
import org.wildfly.swarm.spi.api.Fraction;
import org.wildfly.swarm.spi.api.JARArchive;
import org.wildfly.swarm.spi.api.OutboundSocketBinding;
import org.wildfly.swarm.spi.api.ProjectStage;
import org.wildfly.swarm.spi.api.SocketBinding;
import org.wildfly.swarm.spi.api.SocketBindingGroup;
import org.wildfly.swarm.spi.api.StageConfig;
import org.wildfly.swarm.spi.api.SwarmProperties;
import org.wildfly.swarm.spi.runtime.ServerConfiguration;
import org.wildfly.swarm.spi.runtime.annotations.Post;
import org.wildfly.swarm.spi.runtime.annotations.Pre;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ADD;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.DEFAULT_INTERFACE;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.EXTENSION;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.HOST;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.INET_ADDRESS;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.MULTICAST_ADDRESS;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.MULTICAST_PORT;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP_ADDR;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.PORT;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.PORT_OFFSET;

/**
 * @author Bob McWhirter
 * @author Ken Finnigan
 */
@SuppressWarnings("unused")
@Singleton
@Vetoed
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
    @Any
    private Instance<ArchivePreparer> allArchivePreparers;

    @Inject
    @Any
    private Instance<ArchiveMetadataProcessor> allArchiveProcessors;

    private String defaultDeploymentType;

    public RuntimeServer() {
    }

    @Override
    public void setXmlConfig(Optional<URL> xmlConfig) {
        if (!xmlConfig.isPresent())
            throw new IllegalArgumentException("Invalid XML config");
        this.xmlConfig = xmlConfig;
    }

    @Override
    public void setStageConfig(Optional<ProjectStage> enabledConfig) {
        if (!enabledConfig.isPresent())
            throw new IllegalArgumentException("Invalid stage config");
        this.enabledStage = enabledConfig;
    }

    public void debug(boolean debug) {
        this.debug = debug;
    }

    @Override
    public Deployer start(boolean eagerOpen) throws Exception {

        UUID uuid = UUIDFactory.getUUID();
        System.setProperty("jboss.server.management.uuid", uuid.toString());

        System.err.println("-------------------------------");

        for (Customizer each : this.preCustomizers) {
            System.err.println("PRE CUSTOMZIER: " + each);
            each.customize();
        }

        for (Customizer each : this.postCustomizers) {
            System.err.println("POST CUSTOMZIER: " + each);
            each.customize();
        }

        List<ModelNode> bootstrapOperations = new ArrayList<>();
        this.dmrMarshaller.marshal(bootstrapOperations);

        System.err.println("BOOTSTRAP: " + bootstrapOperations);

        if (LOG.isDebugEnabled()) {
            LOG.debug(bootstrapOperations);
        }

        Thread.currentThread().setContextClassLoader(RuntimeServer.class.getClassLoader());

        UUID grist = java.util.UUID.randomUUID();

        File serverTmp = TempFileManager.INSTANCE.newTempDirectory("wildfly-swarm", ".d");
        System.setProperty("jboss.server.temp.dir", serverTmp.getAbsolutePath());

        ScheduledExecutorService tempFileExecutor = Executors.newSingleThreadScheduledExecutor();
        TempFileProvider tempFileProvider = TempFileProvider.create("wildfly-swarm", tempFileExecutor, true);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                tempFileProvider.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }));

        List<ServiceActivator> activators = new ArrayList<>();

        this.serviceActivators.forEach((activator) -> {
            System.err.println("######## service activator: " + activator);
            activators.add(activator);
        });

        this.serviceContainer = this.container.start(bootstrapOperations, this.contentProvider, activators);
        for (ServiceName serviceName : this.serviceContainer.getServiceNames()) {
            ServiceController<?> serviceController = this.serviceContainer.getService(serviceName);
            StartException exception = serviceController.getStartException();
            if (exception != null) {
                throw exception;
            }
        }

        Opener opener = tryToAddGateHandlers();

        ModelController controller = (ModelController) this.serviceContainer.getService(Services.JBOSS_SERVER_CONTROLLER).getValue();
        Executor executor = Executors.newSingleThreadExecutor();

        this.client = controller.createClient(executor);
        this.deployer = new RuntimeDeployer(opener, this.serviceContainer, this.configList, this.client, this.contentProvider, tempFileProvider);
        this.deployer.debug(this.debug);
        this.deployer.setArchivePreparers(this.allArchivePreparers);
        this.deployer.setArchiveMetadataProcessors(this.allArchiveProcessors);

        this.serviceContainer.addService(ServiceName.of("swarm", "deployer"), new ValueService<>(new ImmediateValue<Object>(this.deployer))).install();

        for (Archive each : this.implicitDeployments) {
            this.deployer.deploy(each);
        }

        setupUserSpaceExtension();

        return this.deployer;
    }

    @Inject
    private Instance<StageConfig> stageConfig;

    private void setupUserSpaceExtension() {
        try {
            Module module = Module.getBootModuleLoader().loadModule(ModuleIdentifier.create("org.wildfly.swarm.cdi", "ext"));
            Class<?> use = module.getClassLoader().loadClass("org.wildfly.swarm.cdi.InjectStageConfigExtension");
            Field field = use.getDeclaredField("stageConfig");
            field.setAccessible(true);
            field.set(null, stageConfig.get());
        } catch (ModuleLoadException e) {
            // ignore, don't do it.
        } catch (ClassNotFoundException | IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
        }

    }

    protected Opener tryToAddGateHandlers() throws Exception {
        try {
            Module undertowRuntime = Module.getBootModuleLoader().loadModule(ModuleIdentifier.create("org.wildfly.swarm.undertow", "runtime"));

            Class<?> wrapperClass = undertowRuntime.getClassLoader().loadClass("org.wildfly.swarm.undertow.runtime.GateHandlerWrapper");

            Object wrapperInstance = wrapperClass.newInstance();

            ServiceName listenerRoot = ServiceName.of("jboss", "undertow", "listener");
            List<ServiceName> names = this.serviceContainer.getServiceNames();

            for (ServiceName name : names) {
                if (listenerRoot.isParentOf(name)) {
                    ServiceController<?> service = this.serviceContainer.getService(name);

                    Object value = service.getValue();
                    Class<?> cls = value.getClass();

                    OUTER:
                    while (cls != null) {
                        Method[] methods = cls.getDeclaredMethods();
                        for (Method method : methods) {
                            if (method.getName().equals("addWrapperHandler")) {
                                method.setAccessible(true);
                                method.invoke(value, wrapperInstance);
                                break OUTER;
                            }
                        }
                        cls = cls.getSuperclass();
                    }
                    service.setMode(ServiceController.Mode.ACTIVE);
                }
            }

            return (Opener) wrapperInstance;
        } catch (ModuleLoadException e) {
            // that's okay, no undertow, quietly return;
        }

        return null;
    }

    public void stop() throws Exception {

        final CountDownLatch latch = new CountDownLatch(1);
        this.serviceContainer.addTerminateListener(info -> latch.countDown());
        this.serviceContainer.shutdown();

        latch.await();

        this.deployer.stop();
        this.serviceContainer = null;
        this.client = null;
        this.deployer = null;
    }

    private void applyInterfaceDefaults(Container config) {
        if (config.ifaces().isEmpty()) {
            config.iface("public",
                         SwarmProperties.propertyVar(SwarmProperties.BIND_ADDRESS, "0.0.0.0"));
        }
    }

    /*
    private void applySocketBindingGroupDefaults(Container config) {
        if (config.socketBindingGroups().isEmpty()) {
            config.socketBindingGroup(
                    new SocketBindingGroup("default-sockets", "public",
                                           SwarmProperties.propertyVar(SwarmProperties.PORT_OFFSET, "0"))
            );
        }

        Set<String> groupNames = config.socketBindings().keySet();

        for (String each : groupNames) {
            //List<SocketBinding> bindings = config.socketBindings().get(each);

            SocketBindingGroup group = config.getSocketBindingGroup(each);
            if (group == null) {
                throw new RuntimeException("No socket-binding-group for '" + each + "'");
            }

            for (SocketBinding binding : this.socketBindings) {
                group.socketBinding( binding );

            }

            //for (SocketBinding binding : bindings) {
                //group.socketBinding(binding);
            //}
        }

        groupNames = config.outboundSocketBindings().keySet();

        for (String each : groupNames) {
            List<OutboundSocketBinding> bindings = config.outboundSocketBindings().get(each);

            SocketBindingGroup group = config.getSocketBindingGroup(each);
            if (group == null) {
                throw new RuntimeException("No socket-binding-group for '" + each + "'");
            }

            for (OutboundSocketBinding binding : bindings) {
                group.outboundSocketBinding(binding);
            }
        }
    }
    */

    private void getSubsystemConfigurations(Container config, List<ModelNode> list) throws Exception {
        if (xmlConfig.isPresent()) {
            configureFractionsFromXML(config, list);
        }
        configureFractions(config, list);
        configureInterfaces(config, list);
        configureSocketBindingGroups(config, list);
    }

    private void configureInterfaces(Container config, List<ModelNode> list) {
        List<Interface> ifaces = config.ifaces();

        for (Interface each : ifaces) {
            configureInterface(each, list);
        }
    }

    private void configureInterface(Interface iface, List<ModelNode> list) {
        if (hasInterface(iface, list)) {
            System.err.println("has interface, not adding");
            return;
        }
        ModelNode node = new ModelNode();

        node.get(OP).set(ADD);
        node.get(OP_ADDR).set("interface", iface.getName());
        node.get(INET_ADDRESS).set(new ValueExpression(iface.getExpression()));

        list.add(node);
    }

    private boolean hasInterface(Interface iface, List<ModelNode> list) {
        return list.stream()
                .anyMatch(e -> {
                    if (!e.get(OP).asString().equals(ADD)) {
                        return false;
                    }

                    ModelNode addr = e.get(OP_ADDR);

                    if (addr.getType() != ModelType.LIST) {
                        return false;
                    }

                    List<ModelNode> addrList = addr.asList();

                    if (addrList.size() != 1) {
                        return false;
                    }

                    Property addrProp = addrList.get(0).asProperty();

                    String propName = addrProp.getName();
                    String propValue = addrProp.getValue().asString();

                    return (propName.equals("interface") && propValue.equals(iface.getName()));
                });
    }

    private void configureSocketBindingGroups(Container config, List<ModelNode> list) {
        List<SocketBindingGroup> groups = config.socketBindingGroups();

        for (SocketBindingGroup each : groups) {
            configureSocketBindingGroup(each, list);
        }
    }

    private void configureSocketBindingGroup(SocketBindingGroup group, List<ModelNode> list) {
        if (hasSocketBindingGroup(list)) {
            return;
        }
        ModelNode node = new ModelNode();

        PathAddress address = PathAddress.pathAddress("socket-binding-group", group.name());
        node.get(OP).set(ADD);
        node.get(OP_ADDR).set(address.toModelNode());
        node.get(DEFAULT_INTERFACE).set(group.defaultInterface());
        node.get(PORT_OFFSET).set(new ValueExpression(group.portOffsetExpression()));
        list.add(node);

        configureSocketBindings(address, group, list);

    }

    private boolean hasSocketBindingGroup(List<ModelNode> list) {
        return list.stream()
                .anyMatch(e -> {
                    if (!e.get(OP).asString().equals(ADD)) {
                        return false;
                    }

                    ModelNode addr = e.get(OP_ADDR);

                    if (addr.getType() != ModelType.LIST) {
                        return false;
                    }
                    List<ModelNode> addrList = addr.asList();

                    if (addrList.size() != 1) {
                        return false;
                    }

                    Property addrProp = addrList.get(0).asProperty();

                    String propName = addrProp.getName();
                    String propValue = addrProp.getValue().asString();

                    return propName.equals("socket-binding-group");
                });
    }

    private void configureSocketBindings(PathAddress address, SocketBindingGroup group, List<ModelNode> list) {
        List<SocketBinding> socketBindings = group.socketBindings();

        for (SocketBinding each : socketBindings) {
            configureSocketBinding(address, each, list);
        }

        List<OutboundSocketBinding> outboundSocketBindings = group.outboundSocketBindings();

        for (OutboundSocketBinding each : outboundSocketBindings) {
            configureSocketBinding(address, each, list);
        }
    }

    private void configureSocketBinding(PathAddress address, SocketBinding binding, List<ModelNode> list) {

        ModelNode node = new ModelNode();

        node.get(OP_ADDR).set(address.append("socket-binding", binding.name()).toModelNode());
        node.get(OP).set(ADD);
        node.get(PORT).set(new ValueExpression(binding.portExpression()));
        if (binding.multicastAddress() != null) {
            node.get(MULTICAST_ADDRESS).set(binding.multicastAddress());
        }
        if (binding.multicastPortExpression() != null) {
            node.get(MULTICAST_PORT).set(new ValueExpression(binding.multicastPortExpression()));
        }

        list.add(node);
    }

    private void configureSocketBinding(PathAddress address, OutboundSocketBinding binding, List<ModelNode> list) {

        ModelNode node = new ModelNode();

        node.get(OP_ADDR).set(address.append("remote-destination-outbound-socket-binding", binding.name()).toModelNode());
        node.get(OP).set(ADD);
        node.get(HOST).set(new ValueExpression(binding.remoteHostExpression()));
        node.get(PORT).set(new ValueExpression(binding.remotePortExpression()));

        list.add(node);
    }

    @SuppressWarnings("unchecked")
    private void configureFractionsFromXML(Container container, List<ModelNode> operationList) throws Exception {

        StandaloneXMLParser parser = new StandaloneXMLParser();

        FractionProcessor<StandaloneXMLParser> consumer = (p, cfg, fraction) -> {
            try {
                cfg.getSubsystemParsers().ifPresent((fractionParsers) -> {
                    ((Map<QName, XMLElementReader<List<ModelNode>>>) fractionParsers).forEach((k, v) -> {
                        try {
                            System.err.println("Registered parser: " + k.getNamespaceURI());
                            parser.addDelegate(k, v);
                        } catch (IllegalArgumentException e) {
                            // ignore, double-add, ignorable
                        }
                    });
                });
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };

        // collect parsers
        visitFractions(container, parser, consumer);

        // parse the configurations
        List<ModelNode> parseResult = parser.parse(xmlConfig.get());
        operationList.addAll(parseResult);
    }

    private void configureFractions(Container config, List<ModelNode> list) throws Exception {
        for (ServerConfiguration<Fraction> eachConfig : this.configList) {
            boolean found = false;
//            for (Fraction eachFraction : config.fractions()) {
//                if (eachConfig.getType().isAssignableFrom(eachFraction.getClass())) {
//                    found = true;
//                    List<ModelNode> subList = eachConfig.getList(eachFraction);
//                    if (!isAlreadyConfigured(subList, list)) {
//                        list.addAll(subList);
//                    }
//                    // else skip because it was configured via XML
//                    break;
//                }
//            }
            if (!found && !eachConfig.isIgnorable()) {
                System.err.println("*** unable to find fraction for: " + eachConfig.getType());
            }
        }
    }

    private boolean isAlreadyConfigured(List<ModelNode> subList, List<ModelNode> list) {
        if (subList.isEmpty()) {
            return false;
        }

        ModelNode head = subList.get(0);

        return list.stream().anyMatch(e -> e.get(OP_ADDR).equals(head.get(OP_ADDR)));
    }

    /**
     * Wraps common iteration pattern over fraction and server configurations
     *
     * @param container The container
     * @param context   processing context (i.e. accumulator)
     * @param fn        a {@link org.wildfly.swarm.container.runtime.RuntimeServer.FractionProcessor} instance
     */
    private <T> void visitFractions(Container container, T context, FractionProcessor<T> fn) {
        for (ServerConfiguration eachConfig : this.configList) {
            boolean found = false;
//            for (Fraction eachFraction : container.fractions()) {
//                if (eachConfig.getType().isAssignableFrom(eachFraction.getClass())) {
//                    found = true;
//                    fn.accept(context, eachConfig, eachFraction);
//                    break;
//                }
//            }
            if (!found && !eachConfig.isIgnorable()) {
                System.err.println("*** unable to find fraction for: " + eachConfig.getType());
            }

        }
    }

    public void deploy() throws DeploymentException {
        Archive<?> deployment = createDefaultDeployment();
        if (deployment == null) {
            throw new DeploymentException("Unable to create default deployment");
        } else {
            deploy(deployment);
        }
    }

    public void deploy(Archive<?> deployment) throws DeploymentException {
        this.deployer.deploy(deployment);
    }

    public Archive<?> createDefaultDeployment() {
        try {
            Iterator<DefaultDeploymentFactory> providerIter = Module.getBootModuleLoader()
                    .loadModule(ModuleIdentifier.create("swarm.application"))
                    .loadService(DefaultDeploymentFactory.class)
                    .iterator();

            if (!providerIter.hasNext()) {
                providerIter = ServiceLoader.load(DefaultDeploymentFactory.class, ClassLoader.getSystemClassLoader())
                        .iterator();
            }

            final Map<String, DefaultDeploymentFactory> factories = new HashMap<>();

            while (providerIter.hasNext()) {
                final DefaultDeploymentFactory factory = providerIter.next();
                final DefaultDeploymentFactory current = factories.get(factory.getType());
                if (current == null) {
                    factories.put(factory.getType(), factory);
                } else {
                    // if this one is high priority than the previously-seen factory, replace it.
                    if (factory.getPriority() > current.getPriority()) {
                        factories.put(factory.getType(), factory);
                    }
                }
            }

            DefaultDeploymentFactory factory = factories.get(determineDeploymentType());
            return factory != null ? factory.create() : ShrinkWrap.create(JARArchive.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String determineDeploymentType() {
        if (this.defaultDeploymentType == null) {
            this.defaultDeploymentType = determineDeploymentTypeInternal();
            System.setProperty(BootstrapProperties.DEFAULT_DEPLOYMENT_TYPE, this.defaultDeploymentType);
        }
        return this.defaultDeploymentType;
    }

    private String determineDeploymentTypeInternal() {
        String artifact = System.getProperty(BootstrapProperties.APP_PATH);
        if (artifact != null) {
            int dotLoc = artifact.lastIndexOf('.');
            if (dotLoc >= 0) {
                return artifact.substring(dotLoc + 1);
            }
        }

        artifact = System.getProperty(BootstrapProperties.APP_ARTIFACT);
        if (artifact != null) {
            int dotLoc = artifact.lastIndexOf('.');
            if (dotLoc >= 0) {
                return artifact.substring(dotLoc + 1);
            }
        }

        // fallback to file system
        FileSystemLayout fsLayout = FileSystemLayout.create();

        return fsLayout.determinePackagingType();
    }

    private static final String BUILD_TIME_INDEX_NAME = "META-INF/swarm-jandex.idx";

    private SelfContainedContainer container = new SelfContainedContainer();

    private SimpleContentProvider contentProvider = new SimpleContentProvider();

    private ServiceContainer serviceContainer;

    private ModelControllerClient client;

    private RuntimeDeployer deployer;

    private Map<Class<? extends Fraction>, ServerConfiguration> configByFractionType = new ConcurrentHashMap<>();

    private List<ServerConfiguration<Fraction>> configList = new ArrayList<>();

    // optional XML config
    private Optional<URL> xmlConfig = Optional.empty();

    private BootstrapLogger LOG = BootstrapLogger.logger("org.wildfly.swarm.runtime.server");

    // TODO : still needed or merge error?
    private boolean debug;

    private Optional<ProjectStage> enabledStage = Optional.empty();

    @FunctionalInterface
    interface FractionProcessor<T> {
        void accept(T t, ServerConfiguration config, Fraction fraction);
    }

    @Vetoed
    private static class ExtensionOpPriorityComparator implements Comparator<ModelNode> {
        @Override
        public int compare(ModelNode left, ModelNode right) {

            PathAddress leftAddr = PathAddress.pathAddress(left.get(OP_ADDR));
            PathAddress rightAddr = PathAddress.pathAddress(right.get(OP_ADDR));

            String leftOpName = left.require(OP).asString();
            String rightOpName = left.require(OP).asString();

            if (leftAddr.size() == 1 && leftAddr.getElement(0).getKey().equals(EXTENSION) && leftOpName.equals(ADD)) {
                return -1;
            }

            if (rightAddr.size() == 1 && rightAddr.getElement(0).getKey().equals(EXTENSION) && rightOpName.equals(ADD)) {
                return 1;
            }

            return 0;
        }
    }

    public interface Opener {
        void open();
    }
}
