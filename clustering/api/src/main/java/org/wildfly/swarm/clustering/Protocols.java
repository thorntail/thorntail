package org.wildfly.swarm.clustering;

/**
 * @author Bob McWhirter
 */
public class Protocols {

    public static Protocol PING() {
        return new Protocol( "PING" );
    }

    public static Protocol MERGE3() {
        return new Protocol( "MERGE3" );
    }

    public static Protocol FD_SOCK(String socketBinding) {
        return new SocketBindingProtocol( "FD_SOCK", socketBinding );
    }

    public static Protocol FD_ALL() {
        return new Protocol( "FD_ALL");
    }

    public static Protocol VERIFY_SUSPECT() {
        return new Protocol( "VERIFY_SUSPECT");
    }

    public static Protocol UNICAST3() {
        return new Protocol( "UNICAST3");
    }

    public static Protocol UFC() {
        return new Protocol( "UFC");
    }

    public static Protocol MFC() {
        return new Protocol( "MFC");
    }

    public static Protocol FRAG2() {
        return new Protocol( "FRAG2");
    }

    public static Protocol RSVP() {
        return new Protocol( "RSVP");
    }

    public static Protocol MPING() {
        return new Protocol( "MPING");
    }

    public static class pbcast {
        public static Protocol STABLE() {
            return new Protocol( "pbcast.STABLE");
        }

        public static Protocol GMS() {
            return new Protocol( "pbcast.GMS");
        }

        public static Protocol NAKACK2() {
            return new Protocol( "pbcast.NAKACK2");
        }

    }
}
