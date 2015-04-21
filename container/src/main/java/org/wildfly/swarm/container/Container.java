package org.wildfly.swarm.container;

import org.jboss.as.controller.ModelController;
import org.jboss.as.controller.client.MessageSeverity;
import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.as.controller.client.OperationMessageHandler;
import org.jboss.as.selfcontained.ContentProvider;
import org.jboss.as.server.SelfContainedContainer;
import org.jboss.as.server.Services;
import org.jboss.dmr.ModelNode;
import org.jboss.msc.service.ServiceContainer;
import org.jboss.msc.service.ServiceController;
import org.jboss.vfs.VirtualFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

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

    private enum State {
        NOT_STARTED,
        STARTING,
        STARTED,
    }

    private List<Subsystem> subsystems = new ArrayList<>();
    private List<SocketBindingGroup> socketBindingGroups = new ArrayList<>();
    private List<Interface> interfaces = new ArrayList<>();

    private SimpleContentProvider contentProvider = new SimpleContentProvider();

    private SelfContainedContainer container;
    private ModelControllerClient client;
    private State state = State.NOT_STARTED;

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

    public synchronized void start() throws Exception {
        this.state = State.STARTING;
        try {
            this.container = new SelfContainedContainer();

            applyDefaults();

            List<ModelNode> list = new ArrayList<>();
            list.addAll(getList());

            ServiceContainer serviceContainer = this.container.start(list, this.contentProvider);
            ModelController controller = (ModelController) serviceContainer.getService(Services.JBOSS_SERVER_CONTROLLER).getValue();
            Executor executor = Executors.newSingleThreadExecutor();
            this.client = controller.createClient(executor);
            this.state = State.STARTED;
        } catch (Exception e) {
            this.state = State.NOT_STARTED;
            throw e;
        }
    }

    public synchronized void start(Deployment deployment) throws Exception {
        start();
        deploy(deployment);
    }

    public synchronized void deploy() throws Exception {
        deploy( new DefaultDeployment() );
    }

    public synchronized void deploy(Deployment deployment) throws Exception {
        if ( this.state != State.STARTED ) {
            start();
        }

        VirtualFile contentFile = deployment.getContent();
        byte[] hash = this.contentProvider.addContent(contentFile);

        final ModelNode deploymentAdd = new ModelNode();

        deploymentAdd.get(OP).set(ADD);
        deploymentAdd.get(OP_ADDR).set("deployment", deployment.getName());
        deploymentAdd.get(RUNTIME_NAME).set(deployment.getName());
        deploymentAdd.get(ENABLED).set(true);

        ModelNode content = deploymentAdd.get(CONTENT).add();
        content.get(HASH).set(hash);

        ModelNode result = client.execute(deploymentAdd);
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
