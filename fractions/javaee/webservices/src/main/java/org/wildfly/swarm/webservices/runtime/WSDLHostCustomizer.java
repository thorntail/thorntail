package org.wildfly.swarm.webservices.runtime;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.wildfly.swarm.container.Interface;
import org.wildfly.swarm.spi.api.Customizer;
import org.wildfly.swarm.spi.runtime.annotations.Post;
import org.wildfly.swarm.webservices.WebServicesFraction;

/**
 * @author Bob McWhirter
 */
@Post
@ApplicationScoped
public class WSDLHostCustomizer implements Customizer {

    @Inject
    Interface iface;

    @Inject
    WebServicesFraction fraction;

    @Override
    public void customize() {
        if (fraction.wsdlHost() == null) {
            fraction.wsdlHost(this.iface.getExpression());
        }
    }
}
