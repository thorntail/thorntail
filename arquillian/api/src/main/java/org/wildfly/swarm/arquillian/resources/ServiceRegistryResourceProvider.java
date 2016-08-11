package org.wildfly.swarm.arquillian.resources;

import java.lang.annotation.Annotation;

import org.jboss.arquillian.container.test.impl.enricher.resource.OperatesOnDeploymentAwareProvider;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.msc.service.ServiceRegistry;
import org.wildfly.swarm.arquillian.adapter.ServiceRegistryServiceActivator;

/**
 * @author Bob McWhirter
 */
public class ServiceRegistryResourceProvider extends OperatesOnDeploymentAwareProvider {


    @Override
    public Object doLookup(ArquillianResource resource, Annotation... qualifiers) {
        return ServiceRegistryServiceActivator.INSTANCE;
    }

    @Override
    public boolean canProvide(Class<?> type) {
        return ServiceRegistry.class.isAssignableFrom( type );
    }
}
