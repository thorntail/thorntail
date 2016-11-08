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
package org.wildfly.swarm.vertx;

import org.wildfly.swarm.spi.api.Configurable;
import org.wildfly.swarm.spi.api.Fraction;
import org.wildfly.swarm.spi.api.Module;
import org.wildfly.swarm.spi.api.annotations.DeploymentModule;

import static org.wildfly.swarm.spi.api.Configurable.*;
import static org.wildfly.swarm.vertx.VertxProperties.DEFAULT_CLUSTER_HOST;
import static org.wildfly.swarm.vertx.VertxProperties.DEFAULT_JNDI_NAME;
import static org.wildfly.swarm.vertx.VertxProperties.DEFAULT_CLUSTER_PORT;

/**
 * @author George Gastaldi
 */
@DeploymentModule(name = "io.vertx.jca", slot = "api")
@DeploymentModule(name = "io.vertx.jca", slot = "ra", services = Module.ServiceHandling.IMPORT )
@DeploymentModule(name = "com.hazelcast" )
public class VertxFraction implements Fraction<VertxFraction> {

    public VertxFraction inhibitAdapterDeployment() {
        this.inhibitAdapterDeployment = true;
        return this;
    }

    public String jndiName() {
        return jndiName.get();
    }

    public VertxFraction jndiName(String jndiName) {
        this.jndiName.set(jndiName);
        return this;
    }

    public String clusterHost() {
        return clusterHost.get();
    }

    public VertxFraction clusterHost(String clusterHost) {
        this.clusterHost.set( clusterHost );
        return this;
    }

    public int clusterPort() {
        return clusterPort.get();
    }

    public VertxFraction clusterPort(int clusterPort) {
        this.clusterPort.set(clusterPort);
        return this;
    }

    public boolean isAdapterDeploymentInhibited() {
        return inhibitAdapterDeployment;
    }

    private boolean inhibitAdapterDeployment;

    private Configurable<String> jndiName = string("swarm.vertx.jndi-name", DEFAULT_JNDI_NAME);

    private Configurable<String> clusterHost = string("swarm.vertx.cluster.host", DEFAULT_CLUSTER_HOST);

    private Configurable<Integer> clusterPort = integer( "swarm.vertx.cluster.port", DEFAULT_CLUSTER_PORT );
}