package org.wildfly.swarm.container;

import org.jboss.dmr.ModelNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

/**
 * @author Bob McWhirter
 */
public class Container {

    private List<Fraction> fractions = new ArrayList<>();
    private List<SocketBindingGroup> socketBindingGroups = new ArrayList<>();
    private List<Interface> interfaces = new ArrayList<>();
    private ContainerProxy container;

    public Container() {

    }

    public Container subsystem(Fraction fraction) {
        this.fractions.add(fraction);
        return this;
    }

    public Container iface(String name, String expression) {
        this.interfaces.add(new Interface(name, expression));
        return this;
    }

    public Container socketBindingGroup(SocketBindingGroup group) {
        this.socketBindingGroups.add(group);
        return this;
    }

    public Container start() {
        this.container = new ContainerProxy();
        try {
            applyDefaults();

            List<ModelNode> list = new ArrayList<>();
            list.addAll(getList());

            this.deployer = this.container.start( list );

            /*
            ServiceContainer serviceContainer = this.container.start(list, this.contentProvider);
            ModelController controller = (ModelController) serviceContainer.getService(Services.JBOSS_SERVER_CONTROLLER).getValue();
            Executor executor = Executors.newSingleThreadExecutor();
            this.client = controller.createClient(executor);
            this.state = State.STARTED;
            */
            return this;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return this;

    }

    private void applyDefaults() throws Exception {
        applyInterfaceDefaults();
        applySocketBindingGroupDefaults();
        applySubsystemDefaults();
    }

    private void applySubsystemDefaults() throws Exception {
        Map<Class<Fraction>, FractionDefaulter> defaulters = new HashMap<>();

        ServiceLoader<FractionDefaulter> loader = ServiceLoader.load(FractionDefaulter.class);
        Iterator<FractionDefaulter> iter = loader.iterator();

        while (iter.hasNext()) {
            FractionDefaulter each = iter.next();
            defaulters.put(each.getSubsystemType(), each);
        }

        for (Fraction each : this.fractions) {
            defaulters.remove(each.getClass());
        }

        for (FractionDefaulter each : defaulters.values()) {
            this.subsystem(each.getDefaultSubsystem());
        }
    }

    private void applyInterfaceDefaults() {
        if (this.interfaces.isEmpty()) {
            iface("public", "${jboss.bind.address:0.0.0.0}");
        }
    }

    private void applySocketBindingGroupDefaults() {
        if (this.socketBindingGroups.isEmpty()) {
            socketBindingGroup(
                    new SocketBindingGroup("default-sockets", "public", "${jboss.socket.binding.port-offset:0}")
                            .socketBinding("http", "${jboss.http.port:8080}")
            );
        }
    }


}
