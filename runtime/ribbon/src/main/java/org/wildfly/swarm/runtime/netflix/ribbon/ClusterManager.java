package org.wildfly.swarm.runtime.netflix.ribbon;

import com.netflix.loadbalancer.Server;
import org.jboss.as.network.SocketBinding;
import org.jboss.msc.inject.Injector;
import org.jboss.msc.service.*;
import org.jboss.msc.value.InjectedValue;
import org.wildfly.clustering.dispatcher.CommandDispatcher;
import org.wildfly.clustering.dispatcher.CommandDispatcherFactory;
import org.wildfly.clustering.group.Group;
import org.wildfly.clustering.group.Node;
import org.wildfly.clustering.group.NodeFactory;
import org.wildfly.swarm.netflix.ribbon.RibbonTopology;

import java.util.ArrayList;
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
    private CommandDispatcher<ClusterManager> dispatcher;

    private Set<String> advertisements = new HashSet<>();
    private Node node;

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
        this.dispatcher = this.commandDispatcherFactoryInjector.getValue().createCommandDispatcher("netflix.ribbon.manager", this );
        this.node = this.commandDispatcherFactoryInjector.getValue().getGroup().getLocalNode();
        requestAdvertisements();
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
        List<Node> removed = new ArrayList<>();
        removed.addAll( previousMembers );
        removed.removeAll( members );
        removed.forEach( (e)->{
            ClusterRegistry.INSTANCE.unregisterAll( nodeKey( e ) );
        });
    }

    protected void requestAdvertisements() {
        try {
            this.dispatcher.submitOnCluster( new RequestAdvertisementsCommand(), this.node );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected synchronized void advertiseAll() {
        for (String each : this.advertisements) {
            doAdvertise(each);
        }
    }

    protected synchronized void advertise(String appName) {
        this.advertisements.add( appName );
        doAdvertise( appName );
    }

    protected void doAdvertise(String appName) {
        SocketBinding binding = this.socketBindingInjector.getValue();
        try {
            this.dispatcher.submitOnCluster(new AdvertiseCommand(nodeKey(this.node), appName, binding.getAddress().getHostAddress(), binding.getAbsolutePort() ) );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected synchronized void unadvertise(String appName) {
        this.advertisements.remove(appName);
        try {
            this.dispatcher.submitOnCluster(new UnadvertiseCommand(nodeKey(this.node), appName));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void register(String nodeKey, String appName, Server server) {
        ClusterRegistry.INSTANCE.register( nodeKey, appName, server );
    }

    void unregister(String nodeKey, String appName) {
        ClusterRegistry.INSTANCE.unregister(nodeKey, appName);
    }

    String nodeKey(Node node) {
        return node.getName() +":" + node.getSocketAddress().toString();

    }

}
