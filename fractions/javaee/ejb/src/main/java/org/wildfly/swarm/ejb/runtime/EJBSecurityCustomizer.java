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
package org.wildfly.swarm.ejb.runtime;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.wildfly.swarm.config.security.Flag;
import org.wildfly.swarm.config.security.SecurityDomain;
import org.wildfly.swarm.config.security.security_domain.ClassicAuthorization;
import org.wildfly.swarm.config.security.security_domain.authorization.PolicyModule;
import org.wildfly.swarm.security.SecurityFraction;
import org.wildfly.swarm.spi.api.Customizer;
import org.wildfly.swarm.spi.runtime.annotations.Post;

/**
 * @author Ken Finnigan
 */
@Post
@Singleton
public class EJBSecurityCustomizer implements Customizer {

    @Inject
    private Instance<SecurityFraction> securityInstance;

    @Override
    public void customize() {
        if (!securityInstance.isUnsatisfied()) {
            SecurityFraction security = securityInstance.get();

            SecurityDomain ejbPolicy = security.subresources().securityDomains().stream().filter((e) -> e.getKey().equals("jboss-ejb-policy")).findFirst().orElse(null);
            if (ejbPolicy == null) {
                ejbPolicy = new SecurityDomain("jboss-ejb-policy")
                        .classicAuthorization(new ClassicAuthorization()
                                                      .policyModule(new PolicyModule("default")
                                                                            .code("Delegating")
                                                                            .flag(Flag.REQUIRED)));
                security.securityDomain(ejbPolicy);
            }
        }
    }
}
