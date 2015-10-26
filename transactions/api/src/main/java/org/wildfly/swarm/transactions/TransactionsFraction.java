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
