package org.wildfly.swarm.container;

import org.jboss.as.controller.ModelController;
import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.as.server.SelfContainedContainer;
import org.jboss.as.server.Services;
import org.jboss.dmr.ModelNode;
import org.jboss.msc.service.ServiceContainer;
import org.jboss.vfs.VirtualFile;

import java.util.ArrayList;
import java.util.Collections;
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

    private List<Fraction> fractions = new ArrayList<>();
    private List<SocketBindingGroup> socketBindingGroups = new ArrayList<>();
    private List<Interface> interfaces = new ArrayList<>();

    private SimpleContentProvider contentProvider = new SimpleContentProvider();

    private SelfContainedContainer container;
    private ModelControllerClient client;
    private State state = State.NOT_STARTED;

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

    public synchronized Container start() throws Exception {
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
            return this;
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
        deploy(new DefaultDeployment());
    }

    public synchronized void deploy(Deployment deployment) throws Exception {
        if (this.state != State.STARTED) {
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

        System.setProperty("wildfly.swarm.current.deployment", deployment.getName());
        ModelNode result = client.execute(deploymentAdd);
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

        Collections.sort(this.fractions, new PriorityComparator());

        for (Fraction each : this.fractions) {
            List<ModelNode> sublist = each.getList();
            list.addAll(sublist);
        }

        return list;
    }
}
