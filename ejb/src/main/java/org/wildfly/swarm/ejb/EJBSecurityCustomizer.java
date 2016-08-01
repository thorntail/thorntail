package org.wildfly.swarm.ejb;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.wildfly.swarm.config.security.Flag;
import org.wildfly.swarm.config.security.SecurityDomain;
import org.wildfly.swarm.config.security.security_domain.ClassicAuthorization;
import org.wildfly.swarm.config.security.security_domain.authorization.PolicyModule;
import org.wildfly.swarm.security.SecurityFraction;
import org.wildfly.swarm.spi.api.Customizer;
import org.wildfly.swarm.spi.api.Post;

/**
 * @author Ken Finnigan
 */
@Post
@ApplicationScoped
public class EJBSecurityCustomizer implements Customizer {
    @Inject
    @Any
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
