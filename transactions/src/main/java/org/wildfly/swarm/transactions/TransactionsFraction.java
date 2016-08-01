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

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;

import org.wildfly.swarm.config.Transactions;
import org.wildfly.swarm.spi.api.DefaultFraction;
import org.wildfly.swarm.spi.api.Fraction;
import org.wildfly.swarm.spi.api.SocketBinding;
import org.wildfly.swarm.spi.api.annotations.DeploymentModule;
import org.wildfly.swarm.spi.api.annotations.MarshalDMR;
import org.wildfly.swarm.spi.api.annotations.WildFlyExtension;

/**
 * @author Bob McWhirter
 */
@ApplicationScoped
@DefaultFraction
@WildFlyExtension(module = "org.jboss.as.transactions")
@MarshalDMR
@DeploymentModule(name = "org.jboss.jts")
public class TransactionsFraction extends Transactions<TransactionsFraction> implements Fraction {

    @PostConstruct
    public void postConstruct() {
        applyDefaults();
    }

    public static TransactionsFraction createDefaultFraction() {
        return new TransactionsFraction().applyDefaults();
    }

    public TransactionsFraction applyDefaults() {
        this.socketBinding("txn-recovery-environment")
                .statusSocketBinding("txn-status-manager")
                .processIdUuid(true);
        return this;
    }

    @Override
    public void initialize(Fraction.InitContext initContext) {

        initContext.socketBinding(new SocketBinding("txn-recovery-environment")
                                          .port(this.port));

        initContext.socketBinding(new SocketBinding("txn-status-manager")
                                          .port(this.statusPort));
    }

    public TransactionsFraction port(int port) {
        this.port = port;
        return this;
    }

    public TransactionsFraction statusPort(int statusPort) {
        this.statusPort = statusPort;
        return this;
    }

    private int port = 4712;

    private int statusPort = 4713;
}
