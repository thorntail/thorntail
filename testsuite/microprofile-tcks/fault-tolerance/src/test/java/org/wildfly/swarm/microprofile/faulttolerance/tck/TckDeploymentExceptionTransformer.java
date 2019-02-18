package org.wildfly.swarm.microprofile.faulttolerance.tck;

import org.jboss.arquillian.container.spi.client.container.DeploymentExceptionTransformer;

public class TckDeploymentExceptionTransformer implements DeploymentExceptionTransformer {
    @Override
    public Throwable transform(Throwable exception) {
        if (exception instanceof org.jboss.weld.exceptions.DefinitionException) {
            Throwable[] suppressed = exception.getSuppressed();
            if (suppressed.length == 1) {
                return suppressed[0];
            }
        }

        return null;
    }
}
