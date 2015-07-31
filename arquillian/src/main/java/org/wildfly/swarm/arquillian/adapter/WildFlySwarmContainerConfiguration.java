package org.wildfly.swarm.arquillian.adapter;

import org.jboss.arquillian.container.spi.ConfigurationException;
import org.jboss.arquillian.container.spi.client.container.ContainerConfiguration;
import org.jboss.arquillian.container.test.api.ContainerController;

/**
 * @author Bob McWhirter
 */
public class WildFlySwarmContainerConfiguration implements ContainerConfiguration {

    public WildFlySwarmContainerConfiguration() {

    }

    @Override
    public void validate() throws ConfigurationException {
    }
}
