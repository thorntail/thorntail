package org.wildfly.swarm.transactions;

import org.wildfly.swarm.config.Transactions;
import org.wildfly.swarm.container.Fraction;

/**
 * @author Bob McWhirter
 */
public class TransactionsFraction extends Transactions<TransactionsFraction> implements Fraction {

    private int port;

    private int statusPort;

    public TransactionsFraction() {

        this(4712, 4713);
        this.socketBinding("txn-recovery-environment")
                .statusSocketBinding("txn-status-manager")
                .processIdUuid(true);

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


    public static TransactionsFraction createDefaultFraction() {
        return new TransactionsFraction();
    }
}
