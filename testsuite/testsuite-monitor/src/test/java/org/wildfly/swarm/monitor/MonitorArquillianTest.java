package org.wildfly.swarm.monitor;

import java.net.URL;
import java.nio.charset.Charset;

import org.apache.commons.io.IOUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.swarm.spi.api.JARArchive;

/**
 * @author Ken Finnigan
 */
@RunWith(Arquillian.class)
public class MonitorArquillianTest {

    @Deployment(testable = false)
    public static Archive createDeployment() {
        JARArchive deployment = ShrinkWrap.create(JARArchive.class);
        deployment.add(EmptyAsset.INSTANCE, "nothing");
        return deployment;
    }

    @RunAsClient
    @Test
    public void testEndpoints() throws Exception {
        System.out.println(IOUtils.toString(new URL("http://127.0.0.1:8080/node"), Charset.forName("UTF-8")));
        System.out.println(IOUtils.toString(new URL("http://127.0.0.1:8080/heap"), Charset.forName("UTF-8")));
        System.out.println(IOUtils.toString(new URL("http://127.0.0.1:8080/threads"), Charset.forName("UTF-8")));
    }
}
