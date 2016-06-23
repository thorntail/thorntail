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
package org.wildfly.swarm.security;

import java.util.HashMap;

import org.wildfly.swarm.config.Security;
import org.wildfly.swarm.config.security.Flag;
import org.wildfly.swarm.config.security.SecurityDomain;
import org.wildfly.swarm.config.security.security_domain.ClassicAuthentication;
import org.wildfly.swarm.config.security.security_domain.authentication.LoginModule;
import org.wildfly.swarm.spi.api.Fraction;
import org.wildfly.swarm.spi.api.annotations.Configuration;
import org.wildfly.swarm.spi.api.annotations.Default;

/**
 * @author Bob McWhirter
 */
@Configuration(
        marshal = true,
        extension = "org.jboss.as.security"
)
public class SecurityFraction extends Security<SecurityFraction> implements Fraction {

    public SecurityFraction() {
    }

    @Default
    public static SecurityFraction defaultSecurityFraction() {
        return new SecurityFraction()
                .securityDomain(new SecurityDomain("other")
                        .classicAuthentication(new ClassicAuthentication()
                                .loginModule(new LoginModule("RealmDirect")
                                        .code("RealmDirect")
                                        .flag(Flag.REQUIRED)
                                        .moduleOptions(new HashMap<Object, Object>() {{
                                            put("password-stacking", "useFirstPass");
                                        }})

                                )));

    }

}
