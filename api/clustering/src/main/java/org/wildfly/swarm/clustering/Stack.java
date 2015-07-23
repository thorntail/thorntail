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
