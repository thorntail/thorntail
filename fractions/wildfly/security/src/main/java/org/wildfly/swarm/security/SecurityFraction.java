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
package org.wildfly.swarm.security;

import org.wildfly.swarm.config.Security;
import org.wildfly.swarm.config.security.Flag;
import org.wildfly.swarm.config.security.SecurityDomain;
import org.wildfly.swarm.spi.api.Fraction;
import org.wildfly.swarm.spi.api.annotations.MarshalDMR;
import org.wildfly.swarm.spi.api.annotations.WildFlyExtension;

/**
 * @author Bob McWhirter
 */
@WildFlyExtension(module = "org.jboss.as.security")
@MarshalDMR
public class SecurityFraction extends Security<SecurityFraction> implements Fraction<SecurityFraction> {

    private static final String DUMMY = "Dummy";

    public static SecurityFraction defaultSecurityFraction() {
        return new SecurityFraction().applyDefaults();
    }

    public SecurityFraction applyDefaults() {
        securityDomain("other", (domain) -> {
            domain.cacheType(SecurityDomain.CacheType.DEFAULT);
            domain.classicAuthentication((auth) -> {
                auth.loginModule("RealmDirect", (module) -> {
                    module.code("RealmDirect");
                    module.flag(Flag.REQUIRED);
                    module.moduleOption("password-stacking", "useFirstPass");
                });
            });
        });

        securityDomain("jaspitest", (domain) -> {
            domain.cacheType(SecurityDomain.CacheType.DEFAULT);
            domain.jaspiAuthentication((auth) -> {
                auth.loginModuleStack("dummy", (stack) -> {
                    stack.loginModule(DUMMY, (module) -> {
                        module.code(DUMMY);
                        module.flag(Flag.OPTIONAL);
                    });
                });
                auth.authModule(DUMMY, (module) -> {
                    module.code(DUMMY);
                });
            });
        });

        return this;
    }

}
