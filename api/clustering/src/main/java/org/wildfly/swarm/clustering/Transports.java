package org.wildfly.swarm.clustering;

/**
 * @author Bob McWhirter
 */
public class Transports {

    public static Transport TCP(String socketBinding) {
        return new Transport("TCP", socketBinding);
    }

    public static Transport UDP(String socketBinding) {
        return new Transport("UDP", socketBinding);
    }
}
