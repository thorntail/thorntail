package org.jboss.unimbus.test.arquillian;

import java.util.Collection;

import org.jboss.arquillian.container.test.spi.TestDeployment;
import org.jboss.arquillian.container.test.spi.client.deployment.DeploymentPackager;
import org.jboss.arquillian.container.test.spi.client.deployment.ProtocolArchiveProcessor;
import org.jboss.shrinkwrap.api.Archive;

/**
 * Created by bob on 1/25/18.
 */
public class UNimbusDeploymentPackager implements DeploymentPackager {

    public Archive<?> generateDeployment(TestDeployment testDeployment, Collection<ProtocolArchiveProcessor> processors) {
        return testDeployment.getApplicationArchive();
    }
}
