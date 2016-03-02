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
package org.wildfly.camel.swarm.core.runtime;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ADD;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.EXTENSION;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP_ADDR;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.SUBSYSTEM;

import java.util.ArrayList;
import java.util.List;

import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.PathElement;
import org.jboss.dmr.ModelNode;
import org.wildfly.camel.swarm.core.CamelCoreFraction;
import org.wildfly.swarm.container.runtime.AbstractServerConfiguration;

public class CamelCoreConfiguration extends AbstractServerConfiguration<CamelCoreFraction> {

    public CamelCoreConfiguration() {
        super(CamelCoreFraction.class);
    }

    @Override
    public CamelCoreFraction defaultFraction() {
        return new CamelCoreFraction();
    }

    @Override
    public List<ModelNode> getList(CamelCoreFraction fraction) {

        ModelNode node = new ModelNode();
        List<ModelNode> list = new ArrayList<>();
        node.get(OP_ADDR).set(EXTENSION, "org.wildfly.extension.camel");
        node.get(OP).set(ADD);
        list.add(node);

        PathAddress address = PathAddress.pathAddress(PathElement.pathElement(SUBSYSTEM, "camel"));

        node = new ModelNode();
        node.get(OP_ADDR).set(address.toModelNode());
        node.get(OP).set(ADD);
        list.add(node);

        return list;
    }
}