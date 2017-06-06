/**
 * Copyright 2015-2016 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wildfly.swarm.container.runtime.marshal;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.jboss.dmr.ModelNode;
import org.jboss.dmr.ModelType;
import org.jboss.dmr.Property;
import org.jboss.dmr.ValueExpression;
import org.wildfly.swarm.container.Interface;
import org.wildfly.swarm.spi.runtime.ConfigurationMarshaller;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ADD;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.INET_ADDRESS;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP_ADDR;

/**
 * @author Bob McWhirter
 */
@ApplicationScoped
public class InterfaceMarshaller implements ConfigurationMarshaller {

    private static final String INTERFACE_NAME = "interface";

    @Inject
    @Any
    private Instance<Interface> interfaces;

    public void marshal(List<ModelNode> list) {

        for (Interface iface : this.interfaces) {
            configureInterface(iface, list);
        }
    }

    private void configureInterface(Interface iface, List<ModelNode> list) {
        if (hasInterface(iface, list)) {
            return;
        }
        ModelNode node = new ModelNode();

        node.get(OP).set(ADD);
        node.get(OP_ADDR).set(INTERFACE_NAME, iface.getName());
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

                    return (propName.equals(INTERFACE_NAME) && propValue.equals(iface.getName()));
                });
    }

}
