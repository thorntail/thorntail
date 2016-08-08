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

import org.wildfly.swarm.spi.api.Fraction;
import org.wildfly.swarm.spi.api.annotations.DeploymentModule;

/**
 * @author George Gastaldi
 */
@DeploymentModule(name = "io.vertx.jca", slot = "api")
public class VertxFraction implements Fraction<VertxFraction> {

    public VertxFraction inhibitAdapterDeployment() {
        this.inhibitAdapterDeployment = true;
        return this;
    }

    public String jndiName() {
        return jndiName;
    }

    public VertxFraction jndiName(String jndiName) {
        this.jndiName = jndiName;
        return this;
    }

    public String clusterHost() {
        return clusterHost;
    }

    public VertxFraction clusterHost(String clusterHost) {
        this.clusterHost = clusterHost;
        return this;
    }

    public int clusterPort() {
        return clusterPort;
    }

    public VertxFraction clusterPort(int clusterPort) {
        this.clusterPort = clusterPort;
        return this;
    }

    public boolean isAdapterDeploymentInhibited() {
        return inhibitAdapterDeployment;
    }

    private boolean inhibitAdapterDeployment;

    private String jndiName = "java:/eis/VertxConnectionFactory";

    private String clusterHost = "localhost";

    private int clusterPort;
}