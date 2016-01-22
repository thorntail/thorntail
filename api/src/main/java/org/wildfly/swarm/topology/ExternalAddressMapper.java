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
package org.wildfly.swarm.topology;

import org.wildfly.swarm.topology.Topology;

public interface ExternalAddressMapper {

    /**
     * Map an internal RibbonServer address to an external one. External in
     * this case means something accessible to a client outside of the
     * internal server's network. RibbonToTheCurbSSEServlet uses this to map
     * RibbonServer addresses to routable addresses accessible to the
     * browser. It's up to the user to supply their own environment-specific
     * implementation if needed.
     *
     * @param internalServer the internal RibbonServer address
     * @param defaultPort port to use if an external port mapping is not found
     * @return the external RibbonServer address
     */
    Topology.Entry toExternal(Topology.Entry internalServer, int defaultPort);

}
