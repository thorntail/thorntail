package org.wildfly.swarm.microprofile.faulttolerance.tck;

import org.jboss.arquillian.container.test.spi.client.deployment.AuxiliaryArchiveAppender;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;

/**
 *
 * @author Martin Kouba
 */
public class FaultToleranceAuxiliaryArchiveAppender implements AuxiliaryArchiveAppender {

    @Override
    public Archive<?> createAuxiliaryArchive() {
        JavaArchive archive = ShrinkWrap.create(JavaArchive.class);
        // https://github.com/eclipse/microprofile-fault-tolerance/issues/369
        archive.addPackages(true, org.hamcrest.Matchers.class.getPackage());
        return archive;
    }

}