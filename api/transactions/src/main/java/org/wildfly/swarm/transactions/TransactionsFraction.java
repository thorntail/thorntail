package org.wildfly.swarm.transactions;

import org.wildfly.swarm.container.Fraction;

/**
 * @author Bob McWhirter
 */
public class TransactionsFraction implements Fraction {

    private int port;

    private int statusPort;

    public TransactionsFraction() {
        this(4712, 4713);
    }

    public TransactionsFraction(int port, int statusPort) {
        this.port = port;
        this.statusPort = statusPort;
    }

    public int getPort() {
        return this.port;
    }

    public int getStatusPort() {
        return this.statusPort;
    }


}
