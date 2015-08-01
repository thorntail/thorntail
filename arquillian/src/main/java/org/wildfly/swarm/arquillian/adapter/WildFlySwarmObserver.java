package org.wildfly.swarm.arquillian.adapter;

import org.jboss.arquillian.container.spi.event.container.AfterSetup;
import org.jboss.arquillian.container.test.impl.client.deployment.event.GenerateDeployment;
import org.jboss.arquillian.core.api.annotation.Observes;

/**
 * @author Bob McWhirter
 */
public class WildFlySwarmObserver {

    private WildFlySwarmContainer container;

    public void afterSetup(@Observes final AfterSetup event) throws Exception {
        this.container = (WildFlySwarmContainer) event.getDeployableContainer();
    }

    public void generate(@Observes(precedence = 100) final GenerateDeployment event) throws Exception {
        this.container.setTestClass( event.getTestClass().getJavaClass() );
    }

}
