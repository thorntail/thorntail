package org.wildfly.swarm.ejb.runtime;

import java.util.ArrayList;
import java.util.List;

import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.PathElement;
import org.jboss.dmr.ModelNode;
import org.wildfly.swarm.ejb.EJBFraction;
import org.wildfly.swarm.runtime.container.AbstractServerConfiguration;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ADD;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.EXTENSION;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP_ADDR;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.SUBSYSTEM;

/**
 * @author Ken Finnigan
 */
public class EJBConfiguration extends AbstractServerConfiguration<EJBFraction> {

    private PathAddress address = PathAddress.pathAddress(PathElement.pathElement(SUBSYSTEM, "ejb3"));

    public EJBConfiguration() {
        super(EJBFraction.class);
    }

    @Override
    public EJBFraction defaultFraction() {
        return new EJBFraction();
    }

    @Override
    public List<ModelNode> getList(EJBFraction fraction) {
        List<ModelNode> list = new ArrayList<>();

        ModelNode node = new ModelNode();
        node.get(OP_ADDR).set(EXTENSION, "org.jboss.as.ejb3");
        node.get(OP).set(ADD);
        list.add(node);

        node = new ModelNode();
        node.get(OP_ADDR).set(address.toModelNode());
        node.get(OP).set(ADD);
        node.get("default-stateful-bean-access-timeout").set(5000);
        node.get("default-sfsb-cache").set("simple");
        node.get("default-sfsb-passivation-disabled-cache").set("simple");
        node.get("default-singleton-bean-access-timeout").set(5000);
        node.get("default-security-domain").set("other");
        node.get("default-missing-method-permissions-deny-access").set("true");
        node.get("log-system-exceptions").set("true");
        list.add(node);

        setBeanPools(list);
        setCache(list);

        node = new ModelNode();
        node.get(OP_ADDR).set(address.append("service", "async").toModelNode());
        node.get(OP).set(ADD);
        node.get("thread-pool-name").set("default");
        list.add(node);

        setTimerService(list);
        setThreadPool(list);

        return list;
    }

    private void setBeanPools(List<ModelNode> list) {
        ModelNode node = new ModelNode();
        node.get(OP_ADDR).set(address.append("strict-max-bean-instance-pool", "slsb-strict-max-pool").toModelNode());
        node.get(OP).set(ADD);
        node.get("max-pool-size").set(20);
        node.get("timeout").set(5);
        node.get("timeout-unit").set("MINUTES");
        list.add(node);

        node = new ModelNode();
        node.get(OP_ADDR).set(address.append("strict-max-bean-instance-pool", "mdb-strict-max-pool").toModelNode());
        node.get(OP).set(ADD);
        node.get("max-pool-size").set(20);
        node.get("timeout").set(5);
        node.get("timeout-unit").set("MINUTES");
        list.add(node);
    }

    private void setCache(List<ModelNode> list) {
        ModelNode node = new ModelNode();
        node.get(OP_ADDR).set(address.append("cache", "simple").toModelNode());
        node.get(OP).set(ADD);
        list.add(node);
    }

    private void setThreadPool(List<ModelNode> list) {
        ModelNode node = new ModelNode();
        node.get(OP_ADDR).set(address.append("thread-pool", "default").toModelNode());
        node.get(OP).set(ADD);
        node.get("max-threads").set(10);
        node.get("keepalive-time").get("time").set(100);
        node.get("keepalive-time").get("unit").set("MILLISECONDS");
        list.add(node);
    }

    private void setTimerService(List<ModelNode> list) {
        ModelNode node = new ModelNode();
        node.get(OP_ADDR).set(address.append("service", "timer-service").toModelNode());
        node.get(OP).set(ADD);
        node.get("thread-pool-name").set("default");
        node.get("default-data-store").set("default-file-store");
        list.add(node);

        node = new ModelNode();
        node.get(OP_ADDR).set(address.append("service", "timer-service").append("file-data-store", "default-file-store").toModelNode());
        node.get(OP).set(ADD);
        node.get("path").set("timer-service-data");
        node.get("relative-to").set("jboss.server.data.dir");
        list.add(node);
    }
}
