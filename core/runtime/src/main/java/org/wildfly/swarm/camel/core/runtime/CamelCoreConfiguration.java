/*
 * #%L
 * Wildfly Swarm :: Camel Core Runtime
 * %%
 * Copyright (C) 2016 RedHat
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package org.wildfly.swarm.camel.core.runtime;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ADD;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.EXTENSION;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP_ADDR;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.SUBSYSTEM;
import static org.wildfly.swarm.camel.core.CamelCoreFraction.LOGGER;

import java.util.ArrayList;
import java.util.List;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.management.mbean.ManagedCamelContext;
import org.apache.camel.model.ModelCamelContext;
import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.PathElement;
import org.jboss.dmr.ModelNode;
import org.wildfly.swarm.camel.core.CamelCoreFraction;
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
    public List<ModelNode> getList(CamelCoreFraction fraction) throws Exception {

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

        for (RouteBuilder builder : fraction.getRouteBuilders()) {

            ModelCamelContext context = builder.getContext();
            builder.addRoutesToCamelContext(context);
            String routesXML = getRoutes(context);

            LOGGER.info("Adding system context: {}", context.getName());
            LOGGER.info("\n{}", routesXML);

            node = new ModelNode();
            address = address.append(PathElement.pathElement("context", context.getName()));
            node.get(OP_ADDR).set(address.toModelNode());
            node.get(OP).set(ADD);

            node.get("value").set(replaceExpressions(routesXML));
            list.add(node);
        }

        return list;
    }

    private String getRoutes(ModelCamelContext context) throws Exception {
        String routesXML = new ManagedCamelContext(context).dumpRoutesAsXml();
        routesXML = routesXML.substring(routesXML.indexOf("<route>"));
        routesXML = routesXML.substring(0, routesXML.lastIndexOf("</route>") + 8);
        return routesXML;
    }

    private String replaceExpressions(String routesXML) {
        return routesXML.replace("${", "#{");
    }
}