package org.wildfly.selfcontained.container;

import org.jboss.as.controller.PathAddress;
import org.jboss.as.server.SelfContainedContainer;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.ValueExpression;

import java.util.ArrayList;
import java.util.List;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ADD;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.CONTENT;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.DEFAULT_INTERFACE;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ENABLED;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.HASH;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.INET_ADDRESS;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP_ADDR;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.PORT_OFFSET;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.RUNTIME_NAME;

/**
 * @author Bob McWhirter
 */
public class Container {

    List<ModelNode> list = new ArrayList<>();
    private List<Subsystem> subsystems = new ArrayList<>();

    private SelfContainedContainer container;

    public Container() {
        //subsystem( new NamingSubsystem() );
        //subsystem( new EeSubsystem() );
        //subsystem( new SecuritySubsystem() );
        //subsystem( new RequestControllerSubsystem() );

    }

    public Container subsystem(Subsystem subsystem) {
        this.subsystems.add(subsystem);
        return this;
    }

    public Container iface(String name, String expression) {
        ModelNode node = new ModelNode();

        node.get(OP).set(ADD);
        node.get(OP_ADDR).set("interface", name);
        node.get(INET_ADDRESS).set(new ValueExpression(expression));

        list.add(node);

        return this;
    }

    public Container socketBindingGroup(SocketBindingGroup group) {
        this.list.addAll( group.getList() );
        return this;
    }

    public void start() {
        this.container = new SelfContainedContainer();
        List<ModelNode> list = new ArrayList<>();
        list.addAll(getList());

        final ModelNode deploymentAdd = new ModelNode();

        deploymentAdd.get(OP).set(ADD);
        deploymentAdd.get(OP_ADDR).set("deployment", "ROOT.war");
        deploymentAdd.get(RUNTIME_NAME).set("ROOT.war");
        deploymentAdd.get(ENABLED).set(true);

        ModelNode content = deploymentAdd.get(CONTENT).add();
        byte[] bytes = new byte[] { 0 };
        content.get(HASH).set( bytes );

        list.add(deploymentAdd);

        this.container.start(list, Content.CONTENT);
    }

    List<ModelNode> getList() {
        List<ModelNode> fullList = new ArrayList<>();

        fullList.addAll(this.list);

        for (Subsystem each : this.subsystems) {
            fullList.addAll(each.getList());
        }

        return fullList;
    }
}
