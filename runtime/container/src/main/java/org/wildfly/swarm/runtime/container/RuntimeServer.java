package org.wildfly.swarm.runtime.container;

import org.jboss.as.controller.ModelController;
import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.as.server.SelfContainedContainer;
import org.jboss.as.server.Services;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.ValueExpression;
import org.jboss.modules.Module;
import org.jboss.modules.ModuleIdentifier;
import org.jboss.modules.ModuleLoadException;
import org.jboss.msc.service.*;
import org.jboss.msc.value.ImmediateValue;
import org.jboss.vfs.TempFileProvider;
import org.wildfly.swarm.container.Container;
import org.wildfly.swarm.container.Deployer;
import org.wildfly.swarm.container.Fraction;
import org.wildfly.swarm.container.Interface;
import org.wildfly.swarm.container.RuntimeModuleProvider;
import org.wildfly.swarm.container.Server;
import org.wildfly.swarm.container.SocketBinding;
import org.wildfly.swarm.container.SocketBindingGroup;

import java.util.*;
import java.util.concurrent.*;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.*;

/**
 * @author Bob McWhirter
 * @author Ken Finnigan
 */
public class RuntimeServer implements Server {

    private SelfContainedContainer container = new SelfContainedContainer();

    private SimpleContentProvider contentProvider = new SimpleContentProvider();

    private ServiceContainer serviceContainer;

    private ModelControllerClient client;

    private RuntimeDeployer deployer;

    private Map<Class<? extends Fraction>, ServerConfiguration> configByFractionType = new ConcurrentHashMap();

    private List<ServerConfiguration> configList = new ArrayList<>();

    public RuntimeServer() throws ModuleLoadException {
        Module loggingModule = Module.getBootModuleLoader().loadModule(ModuleIdentifier.create("org.wildfly.swarm.runtime.logging"));

        ClassLoader originalCl = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(loggingModule.getClassLoader());
            System.setProperty("java.util.logging.manager", "org.jboss.logmanager.LogManager");
            //force logging init
            LogManager.getLogManager();
        } finally {
            Thread.currentThread().setContextClassLoader(originalCl);
        }
    }

    @Override
    public Deployer start(Container config) throws Exception {
        loadFractionConfigurations();

        applyDefaults(config);

        List<ModelNode> list = getList(config);

        // float all <extension> up to the head of the list
        list.sort(new ExtensionOpPriorityComparator());

        Thread.currentThread().setContextClassLoader(RuntimeServer.class.getClassLoader());

        ScheduledExecutorService tempFileExecutor = Executors.newSingleThreadScheduledExecutor();
        TempFileProvider tempFileProvider = TempFileProvider.create("wildfly-swarm", tempFileExecutor);
        List<ServiceActivator> activators = new ArrayList<>();
        activators.add(new ServiceActivator() {
            @Override
            public void activate(ServiceActivatorContext context) throws ServiceRegistryException {
                context.getServiceTarget().addService(ServiceName.of("wildfly", "swarm", "temp-provider"), new ValueService<>(new ImmediateValue<Object>(tempFileProvider)))
                        .install();
            }
        });

        OUTER:
        for (ServerConfiguration eachConfig : this.configList) {
            boolean found = false;
            INNER:
            for (Fraction eachFraction : config.fractions()) {
                if (eachConfig.getType().isAssignableFrom(eachFraction.getClass())) {
                    found = true;
                    activators.addAll(eachConfig.getServiceActivators(eachFraction));
                    break INNER;
                }
            }
            if (!found && !eachConfig.isIgnorable()) {
                System.err.println("*** unable to find fraction for: " + eachConfig.getType());
            }
        }


        this.serviceContainer = this.container.start(list, this.contentProvider, activators);
        ModelController controller = (ModelController) this.serviceContainer.getService(Services.JBOSS_SERVER_CONTROLLER).getValue();
        Executor executor = Executors.newSingleThreadExecutor();

        this.client = controller.createClient(executor);
        this.deployer = new RuntimeDeployer(this.configList, this.client, this.contentProvider, tempFileProvider);

        return this.deployer;
    }

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


    public void stop() throws Exception {

        final CountDownLatch latch = new CountDownLatch(1);
        this.serviceContainer.addTerminateListener(new ServiceContainer.TerminateListener() {
            @Override
            public void handleTermination(Info info) {
                latch.countDown();
            }
        });
        this.serviceContainer.shutdown();

        latch.await();

        this.deployer.stop();
        this.serviceContainer = null;
        this.client = null;
        this.deployer = null;
    }

    @Override
    public Set<Class<? extends Fraction>> getFractionTypes() {
        return this.configByFractionType.keySet();
    }

    @Override
    public Fraction createDefaultFor(Class<? extends Fraction> fractionClazz) {
        return this.configByFractionType.get(fractionClazz).defaultFraction();
    }

    private void applyDefaults(Container config) throws Exception {
        config.applyFractionDefaults(this);
        applyInterfaceDefaults(config);
        applySocketBindingGroupDefaults(config);
    }

    private void applyInterfaceDefaults(Container config) {
        if (config.ifaces().isEmpty()) {
            config.iface("public", "${jboss.bind.address:0.0.0.0}");
        }
    }

    private void applySocketBindingGroupDefaults(Container config) {
        if (config.socketBindingGroups().isEmpty()) {
            config.socketBindingGroup(
                    new SocketBindingGroup("default-sockets", "public", "${jboss.socket.binding.port-offset:0}")
            );
        }

        Set<String> groupNames = config.socketBindings().keySet();

        for (String each : groupNames) {
            List<SocketBinding> bindings = config.socketBindings().get(each);
            SocketBindingGroup group = config.getSocketBindingGroup(each);
            if (group == null) {
                throw new RuntimeException("No socket-binding-group for '" + each + "'");
            }

            for (SocketBinding binding : bindings) {
                group.socketBinding(binding);
            }
        }
    }

    private void loadFractionConfigurations() throws Exception {
        Module m1 = Module.getBootModuleLoader().loadModule(ModuleIdentifier.create("org.wildfly.swarm.bootstrap"));
        ServiceLoader<RuntimeModuleProvider> providerLoader = m1.loadService(RuntimeModuleProvider.class);

        Iterator<RuntimeModuleProvider> providerIter = providerLoader.iterator();

        if (!providerIter.hasNext()) {
            providerLoader = ServiceLoader.load(RuntimeModuleProvider.class, ClassLoader.getSystemClassLoader());
            providerIter = providerLoader.iterator();
        }

        while (providerIter.hasNext()) {
            RuntimeModuleProvider provider = providerIter.next();
            Module module = Module.getBootModuleLoader().loadModule(ModuleIdentifier.create(provider.getModuleName(), provider.getSlotName()));
            ServiceLoader<ServerConfiguration> configLoaders = module.loadService(ServerConfiguration.class);

            for (ServerConfiguration serverConfig : configLoaders) {
                this.configByFractionType.put(serverConfig.getType(), serverConfig);
                this.configList.add(serverConfig);
            }
        }
    }

    private List<ModelNode> getList(Container config) throws ModuleLoadException {
        List<ModelNode> list = new ArrayList<>();

        configureInterfaces(config, list);
        configureSocketBindingGroups(config, list);

        configureFractions(config, list);

        return list;
    }

    private void configureInterfaces(Container config, List<ModelNode> list) {
        List<Interface> ifaces = config.ifaces();

        for (Interface each : ifaces) {
            configureInterface(each, list);
        }
    }

    private void configureInterface(Interface iface, List<ModelNode> list) {
        ModelNode node = new ModelNode();

        node.get(OP).set(ADD);
        node.get(OP_ADDR).set("interface", iface.getName());
        node.get(INET_ADDRESS).set(new ValueExpression(iface.getExpression()));

        list.add(node);
    }

    private void configureSocketBindingGroups(Container config, List<ModelNode> list) {
        List<SocketBindingGroup> groups = config.socketBindingGroups();

        for (SocketBindingGroup each : groups) {
            configureSocketBindingGroup(each, list);
        }
    }

    private void configureSocketBindingGroup(SocketBindingGroup group, List<ModelNode> list) {
        ModelNode node = new ModelNode();

        PathAddress address = PathAddress.pathAddress("socket-binding-group", group.name());
        node.get(OP).set(ADD);
        node.get(OP_ADDR).set(address.toModelNode());
        node.get(DEFAULT_INTERFACE).set(group.defaultInterface());
        node.get(PORT_OFFSET).set(new ValueExpression(group.portOffsetExpression()));
        list.add(node);

        configureSocketBindings(address, group, list);

    }

    private void configureSocketBindings(PathAddress address, SocketBindingGroup group, List<ModelNode> list) {
        List<SocketBinding> bindings = group.socketBindings();

        for (SocketBinding each : bindings) {
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

    private void configureFractions(Container config, List<ModelNode> list) throws ModuleLoadException {

        OUTER:
        for (ServerConfiguration eachConfig : this.configList) {
            boolean found = false;
            INNER:
            for (Fraction eachFraction : config.fractions()) {
                if (eachConfig.getType().isAssignableFrom(eachFraction.getClass())) {
                    found = true;
                    list.addAll(eachConfig.getList(eachFraction));
                    break INNER;
                }
            }
            if (!found && !eachConfig.isIgnorable()) {
                System.err.println("*** unable to find fraction for: " + eachConfig.getType());
            }

        }
        /*
        for (Fraction fraction : config.fractions()) {
            ServerConfiguration serverConfig = this.configByFractionType.get(fraction.getClass());
            if (serverConfig != null) {
                list.addAll(serverConfig.getList(fraction));
            } else {
                for (Class<? extends Fraction> fractionClass : this.configByFractionType.keySet()) {
                    if (fraction.getClass().isAssignableFrom(fractionClass)) {
                        list.addAll(this.configByFractionType.get(fractionClass).getList(fraction));
                        break;
                    }
                }
            }
        }
        */
    }

}
