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
package org.wildfly.swarm.transactions;

import org.wildfly.swarm.config.Transactions;
import org.wildfly.swarm.spi.api.Defaultable;
import org.wildfly.swarm.spi.api.Fraction;
import org.wildfly.swarm.spi.api.annotations.DeploymentModule;
import org.wildfly.swarm.spi.api.annotations.MarshalDMR;
import org.wildfly.swarm.spi.api.annotations.WildFlyExtension;

import static org.wildfly.swarm.spi.api.Defaultable.integer;
import static org.wildfly.swarm.transactions.TransactionsProperties.DEFAULT_PORT;
import static org.wildfly.swarm.transactions.TransactionsProperties.DEFAULT_STATUS_PORT;

/**
 * @author Bob McWhirter
 */
@WildFlyExtension(module = "org.jboss.as.transactions")
@MarshalDMR
@DeploymentModule(name = "org.jboss.jts")
public class TransactionsFraction extends Transactions<TransactionsFraction> implements Fraction<TransactionsFraction> {

    public static TransactionsFraction createDefaultFraction() {
        return new TransactionsFraction().applyDefaults();
    }

    public TransactionsFraction applyDefaults() {
        this.socketBinding("txn-recovery-environment")
                .statusSocketBinding("txn-status-manager")
                .processIdUuid(true);
        return this;
    }

    public TransactionsFraction port(int port) {
        this.port.set(port);
        return this;
    }

    public int port() {
        return this.port.get();
    }

    public TransactionsFraction statusPort(int statusPort) {
        this.statusPort.set(statusPort);
        return this;
    }

    public int statusPort() {
        return this.statusPort.get();
    }

    private Defaultable<Integer> port = integer(DEFAULT_PORT);

    private Defaultable<Integer> statusPort = integer(DEFAULT_STATUS_PORT);
}
