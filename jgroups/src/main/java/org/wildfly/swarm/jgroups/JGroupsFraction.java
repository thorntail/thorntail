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

import org.wildfly.swarm.config.JGroups;
import org.wildfly.swarm.spi.api.Environment;
import org.wildfly.swarm.spi.api.Fraction;
import org.wildfly.swarm.spi.api.SocketBinding;
import org.wildfly.swarm.spi.api.SwarmProperties;
import org.wildfly.swarm.spi.api.annotations.Configuration;
import org.wildfly.swarm.spi.api.annotations.Default;

/**
 * @author Bob McWhirter
 */
@Configuration(
        marshal = true,
        extension = "org.jboss.as.clustering.jgroups"
)
public class JGroupsFraction extends JGroups<JGroupsFraction> implements Fraction {

    public JGroupsFraction() {
    }


    @Default
    public static JGroupsFraction defaultFraction() {
        if (Environment.openshift()) {
            return defaultOpenShiftFraction();
        }
        return defaultMulticastFraction();
    }

    public static JGroupsFraction defaultMulticastFraction() {
        return new JGroupsFraction()
                .defaultChannel("swarm-jgroups")
                .stack("udp", (s) -> {
                    s.transport("UDP", (t) -> {
                        t.socketBinding("jgroups-udp");
                    });
                    s.protocol("PING");
                    s.protocol("MERGE3");
                    s.protocol("FD_SOCK", (p) -> {
                        p.socketBinding("jgroups-udp-fd");
                    });
                    s.protocol("FD_ALL");
                    s.protocol("VERIFY_SUSPECT");
                    s.protocol("pbcast.NAKACK2");
                    s.protocol("UNICAST3");
                    s.protocol("pbcast.STABLE");
                    s.protocol("pbcast.GMS");
                    s.protocol("UFC");
                    s.protocol("MFC");
                    s.protocol("FRAG2");
                    s.protocol("RSVP");
                })
                .channel("swarm-jgroups", (c) -> {
                    c.stack("udp");
                });
    }

    public static JGroupsFraction defaultOpenShiftFraction() {
        return new JGroupsFraction()
                .defaultChannel("swarm-jgroups")
                .stack("tcp", (s) -> {
                    s.transport("TCP", (t) -> {
                        t.socketBinding("jgroups-tcp");
                    });
                    s.protocol("openshift.KUBE_PING");
                    s.protocol("MERGE3");
                    s.protocol("FD_SOCK", (p) -> {
                        p.socketBinding("jgroups-tcp-fd");
                    });
                    s.protocol("FD_ALL");
                    s.protocol("VERIFY_SUSPECT");
                    s.protocol("pbcast.NAKACK2");
                    s.protocol("UNICAST3");
                    s.protocol("pbcast.STABLE");
                    s.protocol("pbcast.GMS");
                    s.protocol("MFC");
                    s.protocol("FRAG2");
                    s.protocol("RSVP");
                })
                .channel("swarm-jgroups", (c) -> {
                    c.stack("tcp");
                });
    }

    @Override
    public void initialize(Fraction.InitContext initContext) {
        initContext.socketBinding(
                new SocketBinding("jgroups-udp")
                        .port(55200)
                        .multicastAddress(SwarmProperties.propertyVar(JGroupsProperties.DEFAULT_MULTICAST_ADDRESS,
                                                                      "230.0.0.4"))
                        .multicastPort(45688));

        initContext.socketBinding(
                new SocketBinding("jgroups-udp-fd")
                        .port(54200));

        initContext.socketBinding(
                new SocketBinding("jgroups-mping")
                        .port(0)
                        .multicastAddress(SwarmProperties.propertyVar(JGroupsProperties.DEFAULT_MULTICAST_ADDRESS,
                                                                      "230.0.0.4"))
                        .multicastPort(45700));

        initContext.socketBinding(
                new SocketBinding("jgroups-tcp")
                        .port(7600));

        initContext.socketBinding(
                new SocketBinding("jgroups-tcp-fd")
                        .port(57600));

    }
}
