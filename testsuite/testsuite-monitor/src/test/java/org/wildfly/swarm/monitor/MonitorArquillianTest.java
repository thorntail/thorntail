package org.wildfly.swarm.monitor;

import org.apache.http.client.fluent.Request;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.swarm.spi.api.JARArchive;

import static org.fest.assertions.Assertions.assertThat;

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

    @Test
    @RunAsClient
    public void testEndpoints() throws Exception {
        {
            String response = Request.Get("http://127.0.0.1:8080/node").execute().returnContent().asString();
            assertThat(response).contains("swarm-version");
        }
        {
            String response = Request.Get("http://127.0.0.1:8080/heap").execute().returnContent().asString();
            assertThat(response).contains("heap-memory-usage");
            assertThat(response).contains("non-heap-memory-usage");
        }
        {
            String response = Request.Get("http://127.0.0.1:8080/threads").execute().returnContent().asString();
            assertThat(response).contains("thread-count");
        }
    }
}
