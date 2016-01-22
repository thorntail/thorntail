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

import org.junit.Ignore;
import org.junit.Test;
import org.wildfly.swarm.SwarmProperties;
import org.wildfly.swarm.container.Container;

/**
 * @author Bob McWhirter
 */
public class TopologyInVmTest {

    @Test
    public void testSimple() throws Exception {
        Container container = new Container();
        container.fraction( new TopologyFraction() );
        container.start().stop();
    }

    @Test
    @Ignore
    public void testCanFindKubePing() throws Exception {
        // TODO: We can't easily test KubePing without a server to hit
        System.setProperty(SwarmProperties.ENVIRONMENT, "openshift");
        Container container = new Container();
        container.start();
        // TODO: something useful here to verify we're actually using KubePing
        container.stop();
        System.setProperty(SwarmProperties.ENVIRONMENT, "");
    }
}
