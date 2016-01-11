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
package org.wildfly.swarm.jgroups;

import org.junit.Test;
import org.wildfly.swarm.container.Container;

public class JGroupsInVmTest {

    @Test
    public void testDefaultFraction() throws Exception {
        Container container = new Container();
        // By not specifying a ClusteringFraction we test the default fraction
        container.start().stop();
    }

    @Test
    public void testCanFindKubePing() throws Exception {
        Container container = new Container();
        container.fraction(new JGroupsFraction()
                .defaultChannel("swarm-jgroups")
                .channel("swarm-jgroups", (c) -> {
                    c.stack("udp");
                })
                .stack("udp", (s) -> {
                    s.transport("UDP", (t) -> {
                        t.socketBinding("jgroups-udp");
                    });
                    s.protocol("openshift.KUBE_PING");
                }));
        container.start().stop();
    }
}
