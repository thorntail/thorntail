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
import org.jboss.msc.service.ServiceContainer;
import org.wildfly.swarm.container.Container;
import org.wildfly.swarm.container.Deployer;
import org.wildfly.swarm.container.Fraction;
import org.wildfly.swarm.container.Interface;
import org.wildfly.swarm.container.RuntimeModuleProvider;
import org.wildfly.swarm.container.Server;
import org.wildfly.swarm.container.SocketBinding;
import org.wildfly.swarm.container.SocketBindingGroup;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ADD;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.DEFAULT_INTERFACE;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.INET_ADDRESS;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP_ADDR;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.PORT;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.PORT_OFFSET;

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

    public RuntimeServer() {
    }

    @Override
    public Deployer start(Container config) throws Exception {
        loadFractionConfigurations();

        applyDefaults(config);

        List<ModelNode> list = getList(config);
        Thread.currentThread().setContextClassLoader(RuntimeServer.class.getClassLoader());
        this.serviceContainer = this.container.start(list, this.contentProvider);
        ModelController controller = (ModelController) this.serviceContainer.getService(Services.JBOSS_SERVER_CONTROLLER).getValue();
        Executor executor = Executors.newSingleThreadExecutor();

        this.client = controller.createClient(executor);

        this.deployer = new RuntimeDeployer(this.client, this.contentProvider);
        return this.deployer;
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
        applyInterfaceDefaults(config);
        applySocketBindingGroupDefaults(config);

        config.applyFractionDefaults(this);
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
                            .socketBinding("http", "${jboss.http.port:8080}")
            );
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
            Module module = Module.getBootModuleLoader().loadModule(ModuleIdentifier.create(provider.getModuleName()));
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
            if ( ! found ) {
                System.err.println( "*** unable to find fraction for: " + eachConfig.getType() );
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
