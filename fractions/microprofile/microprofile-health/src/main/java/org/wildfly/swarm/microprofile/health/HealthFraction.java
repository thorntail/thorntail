/**
 * Copyright 2015-2017 Red Hat, Inc, and individual contributors.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package org.wildfly.swarm.microprofile.health;

import static org.wildfly.swarm.spi.api.Defaultable.string;

import java.util.Optional;

import org.wildfly.swarm.config.runtime.AttributeDocumentation;
import org.wildfly.swarm.spi.api.Defaultable;
import org.wildfly.swarm.spi.api.Fraction;
import org.wildfly.swarm.spi.api.Module;
import org.wildfly.swarm.spi.api.annotations.Configurable;
import org.wildfly.swarm.spi.api.annotations.DeploymentModule;

/**
 * @author Heiko Braun
 */
@DeploymentModule(name = "javax.ws.rs.api")
@DeploymentModule(name = "org.jboss.dmr")
@DeploymentModule(name = "org.wildfly.swarm.microprofile.health")
@DeploymentModule(name = "org.eclipse.microprofile.health", services = Module.ServiceHandling.IMPORT, export = true)
public class HealthFraction implements Fraction<HealthFraction> {

    @AttributeDocumentation("Security realm configuration")
    @Configurable("swarm.microprofile.health.security-realm")
    @Configurable("swarm.health.security-realm")
    private Defaultable<String> securityRealm = string("");

    public HealthFraction securityRealm(String realmName) {
        this.securityRealm.set(realmName);
        return this;
    }

    public Optional<String> securityRealm() {
        return securityRealm.explicit();
    }
}
