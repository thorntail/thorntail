package org.wildfly.swarm.arquillian.adapter;

import org.jboss.arquillian.container.spi.ConfigurationException;
import org.jboss.arquillian.container.spi.client.container.ContainerConfiguration;

/**
 * @author Bob McWhirter
 */
public class WildFlySwarmContainerConfiguration implements ContainerConfiguration {
    @Override
    public void validate() throws ConfigurationException {
    }
}
