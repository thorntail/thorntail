package org.wildfly.swarm.arquillian.adapter;

import java.lang.reflect.Method;

import org.jboss.arquillian.container.spi.client.container.DeployableContainer;
import org.jboss.arquillian.core.spi.LoadableExtension;
import org.jboss.arquillian.test.spi.TestEnricher;
import org.jboss.arquillian.testenricher.ejb.EJBInjectionEnricher;

/**
 * @author Bob McWhirter
 */
public class WildFlySwarmExtension implements LoadableExtension {
    @Override
    public void register(ExtensionBuilder builder) {
        builder.service(DeployableContainer.class, WildFlySwarmContainer.class);
    }
}
