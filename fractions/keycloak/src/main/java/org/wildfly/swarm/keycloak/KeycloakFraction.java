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
package org.wildfly.swarm.keycloak;

import java.util.List;

import org.wildfly.swarm.config.runtime.AttributeDocumentation;
import org.wildfly.swarm.spi.api.Fraction;
import org.wildfly.swarm.spi.api.annotations.Configurable;
import org.wildfly.swarm.spi.api.annotations.DeploymentModule;
import org.wildfly.swarm.spi.api.annotations.WildFlyExtension;
import org.wildfly.swarm.spi.api.annotations.WildFlySubsystem;

/**
 * @author Bob McWhirter
 */
@WildFlyExtension(module = "org.keycloak.keycloak-adapter-subsystem")
@WildFlySubsystem("keycloak")
@DeploymentModule(name = "org.keycloak.keycloak-core")
public class KeycloakFraction implements Fraction<KeycloakFraction> {

    @Configurable("swarm.keycloak.json.path")
    @AttributeDocumentation("Set the external keycloak.json path. If this property specified, keycloak.json on classpath will be ignored")
    String keycloakJsonPath;

    @Configurable("swarm.keycloak.security.constraints")
    @AttributeDocumentation("Set the Security Constraints to protect resources")
    List<String> securityConstraints;

    public String keycloakJsonPath() {
        return keycloakJsonPath;
    }

    public KeycloakFraction keycloakJsonPath(String keycloakJsonPath) {
        this.keycloakJsonPath = keycloakJsonPath;
        return this;
    }

    public List<String> securityConstraints() {
        return securityConstraints;
    }

    public KeycloakFraction securityConstraints(List<String> securityConstraints) {
        this.securityConstraints = securityConstraints;
        return this;
    }

}
