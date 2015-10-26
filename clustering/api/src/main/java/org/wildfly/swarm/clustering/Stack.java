/**
 * Copyright 2015 Red Hat, Inc, and individual contributors.
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
package org.wildfly.swarm.clustering;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Bob McWhirter
 */
public class Stack {

    private final String name;
    private Transport transport;
    private List<Protocol> protocols = new ArrayList<>();

    public Stack(String name) {
        this.name = name;
    }

    public String name() {
        return this.name;
    }

    public Stack transport(Transport transport) {
        this.transport = transport;
       return this;
    }

    public Transport transport() {
        return this.transport;
    }

    public Stack protocol(Protocol protocol) {
        this.protocols.add( protocol );
        return this;
    }

    public List<Protocol> protocols() {
        return this.protocols;
    }

    public static Stack defaultUDPStack() {
        return new Stack("udp")
                .transport(Transports.UDP("jgroups-udp"))
                .protocol(Protocols.PING())
                .protocol(Protocols.FD_SOCK("jgroups-udp-fd"))
                .protocol(Protocols.FD_ALL())
                .protocol(Protocols.VERIFY_SUSPECT())
                .protocol(Protocols.pbcast.NAKACK2())
                .protocol(Protocols.UNICAST3())
                .protocol(Protocols.pbcast.STABLE())
                .protocol(Protocols.pbcast.GMS())
                .protocol(Protocols.UFC())
                .protocol(Protocols.MFC())
                .protocol(Protocols.FRAG2())
                .protocol(Protocols.RSVP());
    }
}
