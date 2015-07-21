package org.wildfly.swarm.runtime.netflix.ribbon;

import org.jboss.as.network.SocketBinding;
import org.jboss.msc.inject.Injector;
import org.jboss.msc.service.*;
import org.jboss.msc.value.InjectedValue;
import org.wildfly.clustering.dispatcher.CommandDispatcher;
import org.wildfly.clustering.dispatcher.CommandDispatcherFactory;
import org.wildfly.clustering.group.Group;
import org.wildfly.clustering.group.Node;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Bob McWhirter
 */
public class ClusterManager implements Service<ClusterManager>, Group.Listener {

    public static final ServiceName SERVICE_NAME = ServiceName.of("netflix", "ribbon", "cluster", "manager");

    private InjectedValue<CommandDispatcherFactory> commandDispatcherFactoryInjector = new InjectedValue<>();
    private InjectedValue<SocketBinding> socketBindingInjector = new InjectedValue<>();
    private CommandDispatcher<ClusterRegistry> dispatcher;

    private Set<String> advertisements = new HashSet<>();

    public ClusterManager() {
    }


    public Injector<CommandDispatcherFactory> getCommandDispatcherFactoryInjector() {
        return this.commandDispatcherFactoryInjector;
    }

    public Injector getSocketBindingInjector() {
        return this.socketBindingInjector;
    }

    @Override
    public void start(StartContext startContext) throws StartException {
        this.commandDispatcherFactoryInjector.getValue().getGroup().addListener(this);
        this.dispatcher = this.commandDispatcherFactoryInjector.getValue().createCommandDispatcher("netflix.ribbon.manager", ClusterRegistry.INSTANCE);
    }

    @Override
    public void stop(StopContext stopContext) {
        this.dispatcher.close();
    }

    @Override
    public ClusterManager getValue() throws IllegalStateException, IllegalArgumentException {
        return this;
    }

    @Override
    public void membershipChanged(List<Node> previousMembers, List<Node> members, boolean merged) {
        advertiseAll();
    }

    public synchronized void advertiseAll() {
        for (String each : this.advertisements) {
            doAdvertise( each );
        }
    }

    public synchronized void advertise(String appName) {
        System.err.println("** advertise: " + appName);
        this.advertisements.add( appName );
        doAdvertise( appName );
    }

    protected void doAdvertise(String appName) {
        SocketBinding binding = this.socketBindingInjector.getValue();
        try {
            this.dispatcher.submitOnCluster(new AdvertiseCommand(appName, binding.getAddress().getHostAddress(), binding.getAbsolutePort() ) );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public synchronized void unadvertise(String appName) {
        this.advertisements.remove(appName);
        try {
            this.dispatcher.submitOnCluster(new UnadvertiseCommand(appName, "localhost", 8080));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
