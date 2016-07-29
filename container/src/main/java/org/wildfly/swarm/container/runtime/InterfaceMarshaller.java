package org.wildfly.swarm.container.runtime;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.jboss.dmr.ModelNode;
import org.jboss.dmr.ModelType;
import org.jboss.dmr.Property;
import org.jboss.dmr.ValueExpression;
import org.wildfly.swarm.container.Interface;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ADD;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.INET_ADDRESS;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP_ADDR;

/**
 * @author Bob McWhirter
 */
public class InterfaceMarshaller {

    @Inject
    @Any
    private Instance<Interface> interfaces;

    public List<ModelNode> marshall() {

        List<ModelNode> list = new ArrayList<>();

        for (Interface iface : this.interfaces) {
            configureInterface( iface, list );
        }

        return list;

    }

    private void configureInterface(Interface iface, List<ModelNode> list) {
        if (hasInterface(iface, list)) {
            System.err.println("has interface, not adding");
            return;
        }
        ModelNode node = new ModelNode();

        node.get(OP).set(ADD);
        node.get(OP_ADDR).set("interface", iface.getName());
        node.get(INET_ADDRESS).set(new ValueExpression(iface.getExpression()));

        list.add(node);
    }

    private boolean hasInterface(Interface iface, List<ModelNode> list) {
        return list.stream()
                .anyMatch(e -> {
                    if (!e.get(OP).asString().equals(ADD)) {
                        return false;
                    }

                    ModelNode addr = e.get(OP_ADDR);

                    if (addr.getType() != ModelType.LIST) {
                        return false;
                    }

                    List<ModelNode> addrList = addr.asList();

                    if (addrList.size() != 1) {
                        return false;
                    }

                    Property addrProp = addrList.get(0).asProperty();

                    String propName = addrProp.getName();
                    String propValue = addrProp.getValue().asString();

                    return (propName.equals("interface") && propValue.equals(iface.getName()));
                });
    }

}
