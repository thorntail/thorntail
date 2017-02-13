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

import org.jboss.dmr.ModelNode;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP_ADDR;

/**
 * @author Bob McWhirter
 */
public interface ConfigurationMarshaller {
    void marshal(List<ModelNode> list);

    default boolean isAlreadyConfigured(List<ModelNode> subList, List<ModelNode> list) {
        if (subList.isEmpty()) {
            return false;
        }

        ModelNode head = subList.get(0);
        ModelNode addr = head.get(OP_ADDR);

        return isAlreadyConfigured(addr, list);
    }

    default boolean isAlreadyConfigured(ModelNode addr, List<ModelNode> list) {
        return list.stream().anyMatch(e -> e.get(OP_ADDR).equals(addr));
    }
}
