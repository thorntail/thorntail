package org.wildfly.swarm.arquillian.deployment;

import javax.enterprise.inject.Vetoed;

import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;
import org.wildfly.swarm.arquillian.daemon.server.Server;

/**
 * @author Bob McWhirter
 */
@Vetoed
public class TestableArchiveService implements Service<Void> {

    public static final ServiceName NAME = ServiceName.of( "wildfly", "swarm", "arquillian", "testable-archive" );

    public TestableArchiveService(String archiveName) {
        this.archiveName = archiveName;
    }


    @Override
    public void start(StartContext startContext) throws StartException {
        this.serverInjector.getValue().setDeploymentUnit( this.deploymentUnitInjector.getValue() );
    }

    @Override
    public void stop(StopContext stopContext) {

    }

    @Override
    public Void getValue() throws IllegalStateException, IllegalArgumentException {
        return null;
    }

    private String archiveName;
    public final InjectedValue<Server> serverInjector = new InjectedValue<>();
    public final InjectedValue<DeploymentUnit> deploymentUnitInjector = new InjectedValue<>();
}
