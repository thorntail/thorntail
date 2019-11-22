package consumer;

import example.Sample;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.swarm.undertow.WARArchive;

import javax.inject.Inject;

import static org.junit.Assert.assertEquals;

/**
 * Created by wlw on 13.09.16.
 */
@RunWith(Arquillian.class)
public class SampleTest {

    @Deployment
    public static Archive createDeployment() throws Exception {
        WARArchive archive = ShrinkWrap.create(WARArchive.class, "SampleTest.war");
        archive.addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
        archive.addPackage("example");
        archive.addAllDependencies();
        return archive;
    }

    @Inject
    private Sample sample;

    @Test
    public void testGet() {
        assertEquals("something", this.sample.saySomething());
    }

}
