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
import org.wildfly.swarm.config.runtime.AttributeDocumentation;
import org.wildfly.swarm.spi.api.Defaultable;
import org.wildfly.swarm.spi.api.Fraction;
import org.wildfly.swarm.spi.api.annotations.Configurable;
import org.wildfly.swarm.spi.api.annotations.MarshalDMR;
import org.wildfly.swarm.spi.api.annotations.WildFlyExtension;

import static org.wildfly.swarm.spi.api.Defaultable.string;

/**
 * @author Bob McWhirter
 */
@WildFlyExtension(module = "org.jboss.as.clustering.jgroups")
@MarshalDMR
public class JGroupsFraction extends JGroups<JGroupsFraction> implements Fraction<JGroupsFraction> {

    public JGroupsFraction() {
    }

    public static JGroupsFraction defaultFraction() {
        return new JGroupsFraction().applyMulticastDefaults();
    }

    public static JGroupsFraction defaultMulticastFraction() {
        return new JGroupsFraction().applyMulticastDefaults();
    }

    @Override
    public JGroupsFraction applyDefaults(boolean hasConfiguration) {
        if (hasConfiguration) {
            return this;
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
                    s.protocol("FD_SOCK");
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

    public JGroupsFraction defaultMulticastAddress(String defaultMulticastAddress) {
        this.defaultMulticastAddress.set(defaultMulticastAddress);
        return this;
    }

    public String defaultMulticastAddress() {
        return this.defaultMulticastAddress.get();
    }

    @AttributeDocumentation("Default multicast address for JGroups")
    @Configurable("thorntail.default.multicast.address")
    private Defaultable<String> defaultMulticastAddress = string("230.0.0.4");

}
