package org.wildfly.swarm.arquillian.deployment;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.stream.Collectors;

import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.Phase;
import org.jboss.as.server.deployment.Services;
import org.jboss.msc.service.ServiceActivator;
import org.jboss.msc.service.ServiceActivatorContext;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.ServiceRegistryException;
import org.wildfly.swarm.arquillian.daemon.server.Server;

/**
 * @author Bob McWhirter
 */
public class TestableArchiveServiceActivator implements ServiceActivator {

    @Override
    public void activate(ServiceActivatorContext context) throws ServiceRegistryException {

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(Thread.currentThread().getContextClassLoader().getResourceAsStream("META-INF/arquillian-testable")))) {

            List<String> lines = reader.lines()
                    .collect(Collectors.toList());

            String archiveName = String.join("", lines).trim();

            TestableArchiveService testableArchiveService = new TestableArchiveService(archiveName);
            context.getServiceTarget()
                    .addService(TestableArchiveService.NAME, testableArchiveService)
                    .addDependency(ServiceName.of("wildfly", "swarm", "arquillian", "daemon"), Server.class, testableArchiveService.serverInjector)
                    .addDependency(Services.deploymentUnitName(archiveName), DeploymentUnit.class, testableArchiveService.deploymentUnitInjector)
                    .addDependency(Services.deploymentUnitName(archiveName, Phase.POST_MODULE))
                    .install();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
