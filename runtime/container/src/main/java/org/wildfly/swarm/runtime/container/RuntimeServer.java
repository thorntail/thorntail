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
import java.util.ServiceLoader;
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
 */
public class RuntimeServer implements Server {

    private SelfContainedContainer container = new SelfContainedContainer();
    private SimpleContentProvider contentProvider = new SimpleContentProvider();

    public RuntimeServer() {
    }

    @Override
    public Deployer start(Container config) throws Exception {
        applyDefaults(config);

        List<ModelNode> list = getList(config);
        Thread.currentThread().setContextClassLoader(RuntimeServer.class.getClassLoader());
        ServiceContainer serviceContainer = this.container.start( list, this.contentProvider );
        ModelController controller = (ModelController) serviceContainer.getService(Services.JBOSS_SERVER_CONTROLLER).getValue();
        Executor executor = Executors.newSingleThreadExecutor();

        ModelControllerClient client = controller.createClient(executor);

        return new RuntimeDeployer( client, this.contentProvider );
    }

    private void applyDefaults(Container config) throws Exception {
        applyInterfaceDefaults(config);
        applySocketBindingGroupDefaults(config);
        applyFractionDefaults(config);
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

    private void applyFractionDefaults(Container config) throws Exception {
        ServiceLoader<ServerConfiguration> loader = ServiceLoader.load(ServerConfiguration.class);
        Iterator<ServerConfiguration> iter = loader.iterator();

        List<Fraction> userInstalledFractions = config.fractions();

        while (iter.hasNext()) {
            ServerConfiguration each = iter.next();

            for (Fraction eachFraction : userInstalledFractions) {
                if (each.getType().equals(eachFraction.getClass())) {
                    break;
                }
            }

            config.fraction(each.defaultFraction());
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

        for ( SocketBinding each : bindings ) {
            configureSocketBinding( address, each, list );
        }
    }

    private void configureSocketBinding(PathAddress address, SocketBinding binding, List<ModelNode> list) {

        ModelNode node = new ModelNode();

        node.get( OP_ADDR ).set( address.append( "socket-binding", binding.name() ).toModelNode() );
        node.get( OP ).set( ADD );
        node.get( PORT ).set( new ValueExpression( binding.portExpression() ) );

        list.add(node);
    }

    private void configureFractions(Container config, List<ModelNode> list) throws ModuleLoadException {
        ServiceLoader<RuntimeModuleProvider> providerLoader = ServiceLoader.load(RuntimeModuleProvider.class);

        Iterator<RuntimeModuleProvider> providerIter = providerLoader.iterator();

        OUTER:
        while ( providerIter.hasNext() ) {
            RuntimeModuleProvider provider = providerIter.next();
            Module module = Module.getBootModuleLoader().loadModule(ModuleIdentifier.create(provider.getModuleName()));
            ServiceLoader<ServerConfiguration> configLoader = module.loadService(ServerConfiguration.class);

            Iterator<ServerConfiguration> configIter = configLoader.iterator();

            MIDDLE:
            while (configIter.hasNext()) {
                ServerConfiguration each = configIter.next();

                INNER:
                for (Fraction fraction : config.fractions()) {
                    if (fraction.getClass().equals(each.getType())) {
                        list.addAll(each.getList(fraction));
                        break INNER;
                    }
                }
            }
        }
    }
}
