package org.wildfly.swarm.microprofile.faulttolerance.asynchronous.future;

import javax.enterprise.inject.spi.Extension;

import org.eclipse.microprofile.faulttolerance.exceptions.FaultToleranceDefinitionException;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.ShouldThrowException;
import org.jboss.arquillian.testng.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.testng.annotations.Test;
import org.wildfly.swarm.microprofile.faulttolerance.HystrixExtension;

/**
 *
 * @author Martin Kouba
 */
public class AsynchronousMethodNotFutureTest extends Arquillian  {

    @ShouldThrowException(FaultToleranceDefinitionException.class)
    @Deployment
    public static JavaArchive createTestArchive() {
        return ShrinkWrap
                .create(JavaArchive.class)
                .addPackage(AsynchronousMethodNotFutureTest.class.getPackage())
                .addAsServiceProvider(Extension.class,HystrixExtension.class)
                .addAsManifestResource(EmptyAsset.INSTANCE,"beans.xml");
    }

    @Test
    public void testIgnored() {
    }
}
