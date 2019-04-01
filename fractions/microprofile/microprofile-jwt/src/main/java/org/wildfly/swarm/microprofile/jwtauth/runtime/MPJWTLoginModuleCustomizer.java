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

import java.io.IOException;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.jboss.modules.ModuleLoadException;
import org.wildfly.swarm.config.security.Flag;
import org.wildfly.swarm.microprofile.jwtauth.MicroProfileJWTAuthFraction;
import org.wildfly.swarm.security.SecurityFraction;
import org.wildfly.swarm.spi.api.Customizer;
import org.wildfly.swarm.spi.runtime.annotations.Post;

@Post
@ApplicationScoped
public class MPJWTLoginModuleCustomizer implements Customizer {

    @Inject
    SecurityFraction securityFraction;

    @Inject
    private MicroProfileJWTAuthFraction mpJwtFraction;

    @Override
    public void customize() throws ModuleLoadException, IOException {
        final String realm = mpJwtFraction.getJwtRealm().get();
        if (!realm.isEmpty() && securityFraction.subresources().securityDomain(realm) == null) {
            securityFraction.securityDomain(realm, (domain) -> {
                domain.jaspiAuthentication((auth) -> {
                    auth.loginModuleStack("roles-lm-stack", (stack) -> {
                        stack.loginModule("0", (module) -> {
                            module.code("org.wildfly.swarm.microprofile.jwtauth.deployment.auth.jaas.JWTLoginModule");
                            module.flag(Flag.REQUIRED);
                            if (mpJwtFraction.getRolesPropertiesMap() != null) {
                                module.moduleOption("rolesProperties", "roles.properties");
                            } else if (!mpJwtFraction.getRolesPropertiesFile().get().isEmpty()) {
                                module.moduleOption("rolesProperties", mpJwtFraction.getRolesPropertiesFile().get());
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
}
