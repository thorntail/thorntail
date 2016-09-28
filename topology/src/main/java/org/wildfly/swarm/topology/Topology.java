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

import java.util.List;
import java.util.Map;

import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 * @author Bob McWhirter
 */
public interface Topology {
    String JNDI_NAME = "swarm/topology";

    static Topology lookup() throws NamingException {
        InitialContext context = new InitialContext();
        return (Topology) context.lookup("jboss/" + Topology.JNDI_NAME);
    }

    void addListener(TopologyListener listener);

    void removeListener(TopologyListener listener);

    AdvertisemetHandle advertise(String name);

    Map<String, List<Entry>> asMap();

    interface Entry {

        String getAddress();

        int getPort();

        List<String> getTags();

    }
}
