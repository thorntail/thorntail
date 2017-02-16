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

import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.jboss.dmr.ModelNode;
import org.wildfly.swarm.internal.SwarmMessages;
import org.wildfly.swarm.spi.api.config.ConfigView;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ADD;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ADDRESS;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.VALUE;

/**
 * @author Bob McWhirter
 */
@ApplicationScoped
public class ConfigViewPropertyMarshaller implements ConfigurationMarshaller {

    @Inject
    private ConfigView configView;

    public void marshal(List<ModelNode> list) {
        Properties properties = this.configView.asProperties();
        Enumeration<?> names = properties.propertyNames();
        while (names.hasMoreElements()) {
            String key = (String) names.nextElement();
            if (!key.startsWith("jboss") && !key.startsWith("java")) {
                String value = properties.getProperty(key);
                if (value != null) {
                    SwarmMessages.MESSAGES.marshalProjectStageProperty(key);
                    ModelNode modelNode = new ModelNode();
                    modelNode.get(OP).set(ADD);
                    modelNode.get(ADDRESS).set("system-property", key);
                    modelNode.get(VALUE).set(properties.getProperty(key));
                    list.add(modelNode);
                }
            }
        }
    }
}
