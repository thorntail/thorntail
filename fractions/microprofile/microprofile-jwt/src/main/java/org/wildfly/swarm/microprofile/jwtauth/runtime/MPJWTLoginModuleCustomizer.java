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
package org.wildfly.swarm.microprofile.jwtauth.runtime;

import static org.wildfly.swarm.spi.api.Defaultable.string;

import java.io.IOException;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.jboss.modules.ModuleLoadException;
import org.wildfly.swarm.config.runtime.AttributeDocumentation;
import org.wildfly.swarm.config.security.Flag;
import org.wildfly.swarm.security.SecurityFraction;
import org.wildfly.swarm.spi.api.Customizer;
import org.wildfly.swarm.spi.api.Defaultable;
import org.wildfly.swarm.spi.api.annotations.Configurable;
import org.wildfly.swarm.spi.runtime.annotations.Post;

@Post
@ApplicationScoped
public class MPJWTLoginModuleCustomizer implements Customizer {

    @Inject
    SecurityFraction security;

    @Override
    public void customize() throws ModuleLoadException, IOException {
        if (!jwtRealm.get().isEmpty() && security.subresources().securityDomain(jwtRealm.get()) == null) {
            security.securityDomain(jwtRealm.get(), (domain) -> {
                domain.jaspiAuthentication((auth) -> {
                    auth.loginModuleStack("roles-lm-stack", (stack) -> {
                        stack.loginModule("0", (module) -> {
                            module.code("org.wildfly.swarm.microprofile.jwtauth.deployment.auth.jaas.JWTLoginModule");
                            module.flag(Flag.REQUIRED);
                            if (!rolesPropertiesFile.get().isEmpty()) {
                                module.moduleOption("rolesProperties", rolesPropertiesFile.get());
                            }
                        });
                    });
                    auth.authModule("http", (module) -> {
                        module.code("org.wildfly.extension.undertow.security.jaspi.modules.HTTPSchemeServerAuthModule");
                        module.module("org.wildfly.extension.undertow");
                        module.flag(Flag.REQUIRED);
                        module.loginModuleStackRef("roles-lm-stack");
                    });
                });
            });
        }
    }

    /**
     * Realm name
     */
    @Configurable("thorntail.microprofile.jwtauth.realm")
    @AttributeDocumentation("If set, a security domain with this name that supports MicroProfile JWT is automatically created"
                             + "in the security subsystem. The realmName parameter of the @LoginConfig annotation must be set to the same value.")
    private Defaultable<String> jwtRealm = string("");

    /**
     * Roles properties file path
     */
    @Configurable("thorntail.microprofile.jwtauth.roles.file")
    @AttributeDocumentation("Roles properties file path")
    private Defaultable<String> rolesPropertiesFile = string("");
}
