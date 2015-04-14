package org.wildfly.boot.container;

import org.jboss.as.server.SelfContainedContainer;
import org.jboss.dmr.ModelNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ADD;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.CONTENT;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ENABLED;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.HASH;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP_ADDR;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.RUNTIME_NAME;

/**
 * @author Bob McWhirter
 */
public class Container {

    private List<Subsystem> subsystems = new ArrayList<>();
    private List<SocketBindingGroup> socketBindingGroups = new ArrayList<>();
    private List<Interface> interfaces = new ArrayList<>();

    private SelfContainedContainer container;

    public Container() {

    }

    public Container subsystem(Subsystem subsystem) {
        this.subsystems.add(subsystem);
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

    public void start() throws Exception {
        start( new DefaultDeployment() );
    }

    public void start(Deployment deployment) throws Exception {
        this.container = new SelfContainedContainer();

        applyDefaults();

        List<ModelNode> list = new ArrayList<>();
        list.addAll(getList());

        final ModelNode deploymentAdd = new ModelNode();

        deploymentAdd.get(OP).set(ADD);
        deploymentAdd.get(OP_ADDR).set("deployment", "ROOT.war");
        deploymentAdd.get(RUNTIME_NAME).set("ROOT.war");
        deploymentAdd.get(ENABLED).set(true);

        ModelNode content = deploymentAdd.get(CONTENT).add();
        byte[] bytes = new byte[]{0};
        content.get(HASH).set(bytes);

        list.add(deploymentAdd);

        this.container.start(list, deployment.getContent() );
    }

    private void applyDefaults() throws Exception {
        applyInterfaceDefaults();
        applySocketBindingGroupDefaults();
        applySubsystemDefaults();
    }

    private void applySubsystemDefaults() throws Exception {
        Map<Class<Subsystem>, SubsystemDefaulter> defaulters = new HashMap<>();

        ServiceLoader<SubsystemDefaulter> loader = ServiceLoader.load(SubsystemDefaulter.class);
        Iterator<SubsystemDefaulter> iter = loader.iterator();

        while (iter.hasNext()) {
            SubsystemDefaulter each = iter.next();
            defaulters.put(each.getSubsystemType(), each);
        }

        for (Subsystem each : this.subsystems) {
            defaulters.remove(each.getClass());
        }

        for (SubsystemDefaulter each : defaulters.values()) {
            this.subsystem(each.getDefaultSubsystem());
        }
    }

    private void applyInterfaceDefaults() {
        if (this.interfaces.isEmpty()) {
            iface("public", "${jboss.bind.address:127.0.0.1}");
        }
    }

    private void applySocketBindingGroupDefaults() {
        if (this.socketBindingGroups.isEmpty()) {
            socketBindingGroup(
                    new SocketBindingGroup("default-sockets", "public", "0")
                            .socketBinding("http", 8080)
            );
        }
    }

    List<ModelNode> getList() {
        List<ModelNode> list = new ArrayList<>();

        for (Interface each : this.interfaces) {
            list.add(each.getNode());
        }

        for (SocketBindingGroup each : this.socketBindingGroups) {
            list.addAll(each.getList());
        }

        for (Subsystem each : this.subsystems) {
            list.addAll(each.getList());
        }

        return list;
    }
}
