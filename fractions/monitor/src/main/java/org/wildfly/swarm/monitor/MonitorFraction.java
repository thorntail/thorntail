/**
 * Copyright 2015-2017 Red Hat, Inc, and individual contributors.
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
package org.wildfly.swarm.monitor;

import java.util.Optional;

import org.wildfly.swarm.spi.api.Fraction;
import org.wildfly.swarm.spi.api.annotations.DeploymentModule;

/**
 * @author Heiko Braun
 */
@DeploymentModule(name = "javax.ws.rs.api")
@DeploymentModule(name = "org.jboss.dmr")
@DeploymentModule(name = "org.wildfly.swarm.monitor")
@DeploymentModule(name = "org.wildfly.swarm.monitor", slot = "deployment", export = true, metaInf = DeploymentModule.MetaInfDisposition.IMPORT)
@DeploymentModule(name = "org.eclipse.microprofile.health")
public class MonitorFraction implements Fraction<MonitorFraction> {

    private Optional<String> securityRealm = Optional.empty();

    public MonitorFraction securityRealm(String realmName) {
        this.securityRealm = Optional.of(realmName);
        return this;
    }

    public Optional<String> securityRealm() {
        return this.securityRealm;
    }
}
