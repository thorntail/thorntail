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

import javax.annotation.PostConstruct;
import javax.inject.Singleton;

import org.wildfly.swarm.config.JGroups;
import org.wildfly.swarm.spi.api.DefaultFraction;
import org.wildfly.swarm.spi.api.Environment;
import org.wildfly.swarm.spi.api.Fraction;
import org.wildfly.swarm.spi.api.annotations.MarshalDMR;
import org.wildfly.swarm.spi.api.annotations.WildFlyExtension;

/**
 * @author Bob McWhirter
 */
@Singleton
@WildFlyExtension(module = "org.jboss.as.clustering.jgroups")
@MarshalDMR
@DefaultFraction
public class JGroupsFraction extends JGroups<JGroupsFraction> implements Fraction {

    public JGroupsFraction() {
    }

    @PostConstruct
    public void postConstruct() {
        applyDefaults();
    }

    public static JGroupsFraction defaultFraction() {
        return new JGroupsFraction().applyDefaults();
    }

    public static JGroupsFraction defaultMulticastFraction() {
        return new JGroupsFraction().applyMulticastDefaults();
    }

    public static JGroupsFraction defaultOpenShiftFraction() {
        return new JGroupsFraction().applyOpenShiftDefaults();
    }

    public JGroupsFraction applyDefaults() {
        if (Environment.openshift()) {
            return applyOpenShiftDefaults();
        }
        return applyMulticastDefaults();
    }


    public JGroupsFraction applyMulticastDefaults() {
        return defaultChannel("swarm-jgroups")
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

    public JGroupsFraction applyOpenShiftDefaults() {
        return defaultChannel("swarm-jgroups")
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

    public JGroupsFraction defaultMulticastAddress(String defaultMulticastAddress) {
        this.defaultMulticastAddress = defaultMulticastAddress;
        return this;
    }

    public String defaultMulticastAddress() {
        return this.defaultMulticastAddress;
    }

    private String defaultMulticastAddress = "230.0.0.4";

}
