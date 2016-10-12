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
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.jboss.dmr.ModelNode;
import org.wildfly.swarm.internal.SwarmMessages;
import org.wildfly.swarm.spi.api.ProjectStage;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ADD;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ADDRESS;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.VALUE;

/**
 * @author Bob McWhirter
 */
@Singleton
public class ProjectStagePropertyMarshaller implements ConfigurationMarshaller {

    @Inject
    private ProjectStage stage;

    public void marshal(List<ModelNode> list) {
        Map<String, String> properties = this.stage.getProperties();
        for (String key : properties.keySet()) {
            SwarmMessages.MESSAGES.marshalProjectStageProperty(key);
            ModelNode modelNode = new ModelNode();
            modelNode.get(OP).set(ADD);
            modelNode.get(ADDRESS).set("system-property", key);
            modelNode.get(VALUE).set(properties.get(key));
            list.add(modelNode);
        }
    }
}
