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
package org.wildfly.swarm.container.runtime;

import java.util.UUID;

import javax.enterprise.inject.Vetoed;

import org.wildfly.swarm.spi.api.internal.SwarmInternalProperties;

/**
 * @author Bob McWhirter
 */
@Vetoed
public class UUIDFactory {

    public static UUID getUUID() {


        String swarmNodeId = System.getProperty(SwarmInternalProperties.NODE_ID);
        String jbossNodeName = System.getProperty("jboss.node.name");

        String uuidInput = null;

        // Prefer swarm.node.id, if present and jboss.node.name is
        // not, then set jboss.node.name=swarm.node.id
        if (swarmNodeId != null) {
            uuidInput = swarmNodeId;
            if (jbossNodeName == null) {
                System.setProperty("jboss.node.name", swarmNodeId);
            }
        }
        if (jbossNodeName != null) {
            uuidInput = jbossNodeName;
        }

        // if neither swarm.node.id nor jboss.node.name are set,
        // just generate a random UUID
        if (uuidInput == null) {
            return UUID.randomUUID();
        }

        return UUID.nameUUIDFromBytes(uuidInput.getBytes());
    }
}
