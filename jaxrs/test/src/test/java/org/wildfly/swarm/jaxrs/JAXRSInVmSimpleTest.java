package org.wildfly.swarm.jaxrs;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.junit.Assert;
import org.junit.Test;
import org.wildfly.swarm.container.Container;
/**
 * @author Heiko Braun
 * @since 29/03/16
 */
public class JAXRSInVmSimpleTest extends SimpleHttp {

    @Test
    public void testSimple() throws Exception {

        Container container = new Container();
        container.fraction(new JAXRSFraction());
        container.start();

        JAXRSArchive deployment = ShrinkWrap.create(JAXRSArchive.class, "myapp.war");
        deployment.addClass(TimeResource.class);

        container.deploy(deployment);

        Response response = getUrlContents("http://localhost:8080/another-app/time", true);
        Assert.assertTrue(response.getBody().contains("Time"));

        container.stop();
    }

}
